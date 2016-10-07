package com.ge.predix.solsvc.machinedata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.dspmicro.hoover.api.processor.IProcessor;
import com.ge.dspmicro.hoover.api.processor.ProcessorException;
import com.ge.dspmicro.hoover.api.spillway.ITransferData;
import com.ge.dspmicro.machinegateway.types.ITransferable;
import com.ge.dspmicro.websocketriver.send.api.IWebsocketSend;
import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;

@Component(name = DataExchangeConnector.SERVICE_PID)
public class DataExchangeConnector implements IProcessor {
	/**
	 * Create logger to report errors, warning massages, and info messages
	 * (runtime Statistics)
	 */
	protected static Logger _logger = LoggerFactory.getLogger(DataExchangeConnector.class);

	/** Service PID for Sample Machine Adapter */
	public static final String SERVICE_PID = "com.ge.predix.solsvc.machinedata.processor"; //$NON-NLS-1$

	//private JsonMapper mapper = new JsonMapper();
	
	private IWebsocketSend wsRiverSend;

	private static final int RECEIVE_TIMEOUT = 5000;

	/** Lock object to sync the async call and callback. */
	private static Object _syncLock = new Object();

	@Activate
	public void activate(ComponentContext ctx) throws IOException {
		
	}

	@Override
	public void processValues(String processType, Map<String, String> properties, List<ITransferable> values,
			ITransferData transferData) throws ProcessorException {

		if (properties != null) {
			System.out.println("");
		}
		_logger.info("VALUES :" + values.toString()); //$NON-NLS-1$
		DatapointsIngestion datapointsIngestion = createTimeseriesDataBody(values);
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		PutFieldDataCriteria criteria = new PutFieldDataCriteria();
		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		
		fieldIdentifier.setSource(FieldSourceEnum.PREDIX_TIMESERIES.name());
		field.setFieldIdentifier(fieldIdentifier);
		List<Field> fields = new ArrayList<Field>();
		fields.add(field);
		fieldData.setField(fields);
		PredixString predixString = new PredixString();
		try {
			ObjectMapper mapper = new ObjectMapper();
			predixString.setString(mapper.writeValueAsString(datapointsIngestion));
		} catch (JsonProcessingException e1) {
			e1.printStackTrace();
		}
		fieldData.setData(predixString);
		criteria.setFieldData(fieldData);
		List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
		list.add(criteria);
		putFieldDataRequest.setPutFieldDataCriteria(list);
		
		try ( ByteArrayInputStream pDataInputStream = constructByteStream(putFieldDataRequest) )
        {
            _logger.info("Sending test data to cloud."); //$NON-NLS-1$
            // Send the data
            
            synchronized (_syncLock)
            {
            	this.wsRiverSend.transfer(pDataInputStream, null);
                _syncLock.wait(RECEIVE_TIMEOUT); // Callback will notify when transfer completes
            }
        } catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void processValues(String processType, List<ITransferable> values, ITransferData transferData)
			throws ProcessorException {
		this.processValues(processType, null, values, transferData);
	}

	@SuppressWarnings("unchecked")
	private DatapointsIngestion createTimeseriesDataBody(List<ITransferable> values) {
		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(UUID.randomUUID().toString());
		List<Body> bodies = new ArrayList<Body>();
		for (ITransferable t : values) {
			SampleDataValue sdv = (SampleDataValue) t;
			Body body = new Body();
			List<Object> datapoints = new ArrayList<Object>();
			body.setName(sdv.getNodeName());

			// attributes
			com.ge.predix.entity.util.map.Map map = new com.ge.predix.entity.util.map.Map();
			map.put("assetId", sdv.getAssetId());

			map.put("sourceTagId", sdv.getNodeName());

			body.setAttributes(map);

			// datapoints
			List<Object> datapoint = new ArrayList<Object>();
			datapoint.add(converLocalTimeToUtcTime(sdv.getTimestamp().getTimeMilliseconds()));
			datapoint.add(sdv.getValue().getValue());
			datapoints.add(datapoint);

			body.setDatapoints(datapoints);
			bodies.add(body);
		}
		dpIngestion.setBody(bodies);

		return dpIngestion;
	}

	private long converLocalTimeToUtcTime(long timeSinceLocalEpoch) {
		return timeSinceLocalEpoch + getLocalToUtcDelta();
	}

	private long getLocalToUtcDelta() {
		Calendar local = Calendar.getInstance();
		local.clear();
		local.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
		return local.getTimeInMillis();
	}

	/**
	 * Dependency injection of WebSocket River Send service.
	 * 
	 * @param sender
	 *            OSGi registered implementation of IWebsocketSend interface.
	 */
	@Reference
	protected void setWsRiverSend(IWebsocketSend sender) {
		this.wsRiverSend = sender;
	}

	/**
	 * Remove this WebSocketRiver Send service from dependency injection
	 * 
	 * @param sender
	 *            OSGi registered implementation of IWebsocketSend interface.
	 */
	protected void unsetWsRiverSend(IWebsocketSend sender) {
		this.wsRiverSend = null;
	}
	
	private ByteArrayInputStream constructByteStream(PutFieldDataRequest putFieldDataRequest)
            throws IOException
    {
		ObjectMapper mapper = new ObjectMapper();
        return new ByteArrayInputStream(mapper.writeValueAsBytes(putFieldDataRequest));
    }
}
