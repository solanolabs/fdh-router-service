/*
 * Copyright (c) 2016 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.handler.timeseries;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ge.predix.entity.datafile.DataFile;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.Body;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.entity.util.map.Entry;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.timeseries.bootstrap.config.TimeseriesRestConfig;
import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;

/**
 * 
 * @author 212421693
 */
@Component("timeSeriesPutExecutor")
public class TimeSeriesPutExecutor {

	private static final Logger logger = LoggerFactory.getLogger(TimeSeriesPutExecutor.class);

	@Autowired
	private TimeseriesFactory timeseriesFactory;

	@Autowired
	private TimeseriesRestConfig timeseriesRestConfig;

	/**
	 * @param putFieldDataRequest -
	 * @param putFieldDataResult -
	 * @param headers  -
	 */
	// @Async
	public void putFieldDataProcessor(PutFieldDataRequest putFieldDataRequest, PutFieldDataResult putFieldDataResult,
			List<Header> headers) {
		String threadName = Thread.currentThread().getName();
		String uuidId = ""; //$NON-NLS-1$
		List<Entry> entries = putFieldDataResult.getExternalAttributeMap().getEntry();
		for (Entry entry : entries) {
			if (org.apache.commons.lang.StringUtils.endsWithIgnoreCase(entry.getKey().toString(), "UUID")) { //$NON-NLS-1$
				uuidId = entry.getValue().toString();
			}

		}

		logger.info("UUID :" + uuidId + "   " + threadName + " has began working."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		List<PutFieldDataCriteria> fieldDataCriteria = putFieldDataRequest.getPutFieldDataCriteria();
		for (PutFieldDataCriteria putFieldDataCriteria : fieldDataCriteria) {
			if (putFieldDataCriteria.getFieldData().getData() instanceof DataFile) {
				DataFile datafile = (DataFile) putFieldDataCriteria.getFieldData().getData();
				MultipartFile file = (MultipartFile) datafile.getFile();

				logger.info("UUID :" + uuidId + "   working...to upload the file = " + file.getOriginalFilename()); //$NON-NLS-1$ //$NON-NLS-2$

				headers.add(new BasicHeader("Origin", "http://predix.io")); //$NON-NLS-1$ //$NON-NLS-2$
				headers.add(new BasicHeader(this.timeseriesRestConfig.getPredixZoneIdHeaderName(),
						this.timeseriesRestConfig.getZoneId()));

				this.timeseriesFactory.createConnectionToTimeseriesWebsocket();
				if (!StringUtils.isEmpty(file.getOriginalFilename())
						&& file.getOriginalFilename().toLowerCase().endsWith("csv")) { //$NON-NLS-1$
					// process csv file
					processUploadCsv(headers, file, uuidId);
				}
				logger.info("UUID :" + uuidId + "   " + threadName + " has completed work."); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			} else if (putFieldDataCriteria.getFieldData().getData() instanceof PredixString) {
				PredixString ps = (PredixString) putFieldDataCriteria.getFieldData().getData();
				JsonMapper jsonMapper = new JsonMapper();
				DatapointsIngestion datapointsIngestion = jsonMapper.fromJson(ps.getString(),
						DatapointsIngestion.class);
				this.timeseriesFactory.createConnectionToTimeseriesWebsocket();
				this.timeseriesFactory.postDataToTimeseriesWebsocket(datapointsIngestion);
			}
		}

	}

	/**
	 * @param headers
	 *            -
	 * @param file
	 *            -
	 * @param uuid
	 *            -
	 */

	@SuppressWarnings("resource")
	void processUploadCsv(List<Header> headers, MultipartFile file, String uuid) {

		DatapointsIngestion dpIngestion = new DatapointsIngestion();
		dpIngestion.setMessageId(String.valueOf(System.currentTimeMillis()));
		List<Body> bodies = new ArrayList<Body>();
		SimpleDateFormat df = null;

		CSVParser csvFileParser = null;
		try {
			csvFileParser = CSVFormat.EXCEL.withHeader().parse(new InputStreamReader(file.getInputStream()));

			List<CSVRecord> csvRecords = csvFileParser.getRecords();
			Map<String, Integer> headerMap = csvFileParser.getHeaderMap();
			for (CSVRecord csvRecord : csvRecords) {

				for (String name : headerMap.keySet()) {
					Body body = new Body();
					List<Object> datapoint = new ArrayList<Object>();
					String key = name.toString();
					String value = csvRecord.get(key);
					if (StringUtils.startsWithIgnoreCase(key, "Date") //$NON-NLS-1$
							&& df == null) {
						String dataFormattedString = StringUtils.replace(key, "Date(", ""); //$NON-NLS-1$//$NON-NLS-2$
						String dataformat = StringUtils.replace(dataFormattedString, ")", ""); //$NON-NLS-1$//$NON-NLS-2$
						df = new SimpleDateFormat(dataformat);
					}
					if (StringUtils.startsWithIgnoreCase(key, "Date")) { //$NON-NLS-1$
						@SuppressWarnings("null")
						Date date = df.parse(value);
						long epoch = date.getTime();
						datapoint.add(epoch);
					} else {
						datapoint.add(value);
						body.setName(key);

						bodies.add(body);
					}
					body.setDatapoints(datapoint);
				}

				dpIngestion.setBody(bodies);
				logger.trace("UUID :" + uuid + " injection" + dpIngestion.toString()); //$NON-NLS-1$ //$NON-NLS-2$
				this.timeseriesFactory.postDataToTimeseriesWebsocket(dpIngestion);
			} // record close

			logger.info("UUID :" + uuid + " # records are " + csvRecords.size()); //$NON-NLS-1$ //$NON-NLS-2$

		} catch (IOException | ParseException e) {
			logger.error("UUID :" + uuid + " Error processing upload response ", e); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			csvFileParser = null;
		}

	}
}
