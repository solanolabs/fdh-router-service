package com.ge.predix.solsvc.fdh.router.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.source.Source;
import com.ge.predix.entity.timeseries.datapoints.ingestionrequest.DatapointsIngestion;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.router.service.router.PutRouter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * @author predix.adoption@ge.com -
 */
@ServerEndpoint(value = "/livestream/{nodeId}",configurator=FdhWebSocketServerConfig.class)
public class FdhWebSocketServerEndPoint implements ApplicationContextAware{
	
	private static Logger logger = LoggerFactory.getLogger(FdhWebSocketServerEndPoint.class);

	private static final LinkedList<Session> clients = new LinkedList<Session>();
	
	@Autowired
	private PutRouter putFieldDataService;
	
	private String nodeId;
	
	@PostConstruct
	public void init(){
	    SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	/**
	 * @param nodeId1 - nodeId for the session
	 * @param session - session object
	 * @param ec
	 *            -
	 */
	@OnOpen
	public void onOpen(@PathParam(value = "nodeId") String nodeId1, final Session session, EndpointConfig ec) {
		//logger.info("headers : "+request.getHeaders()); //$NON-NLS-1$
		this.nodeId = nodeId1;
		clients.add(session);
		logger.info("Server: opened... for Node Id : " + nodeId1 + " : " + session.getId()); //$NON-NLS-1$ //$NON-NLS-2$
		logger.info("Nunmber of open connections : " + session.getOpenSessions().size()); //$NON-NLS-1$
	}

	/**
	 * @param nodeId -
	 * @param message -
	 * @param session -
	 */
	@SuppressWarnings("unchecked")
	@OnMessage
	public void onMessage(String message, Session session) {
		logger.info("Websocket Message : " + message); //$NON-NLS-1$
		
		logger.info("RequestParameterMap : "+session.getUserProperties()); //$NON-NLS-1$
		try {
			if ("messages".equalsIgnoreCase(this.nodeId)) { //$NON-NLS-1$
				System.out.println("No of opensessions : " + clients.size()); //$NON-NLS-1$
				JsonMapper mapper = new JsonMapper();
				DatapointsIngestion timeSeriesRequest = mapper.fromJson(message, DatapointsIngestion.class);
				PutFieldDataRequest putFieldDataRequest = null;
				if (timeSeriesRequest != null && timeSeriesRequest.getMessageId() != null) {
					putFieldDataRequest = new PutFieldDataRequest();
					PutFieldDataCriteria criteria = new PutFieldDataCriteria();
					FieldData fieldData = new FieldData();
					Field field = new Field();
					FieldIdentifier  fieldIdentifier = new FieldIdentifier();
					fieldIdentifier.setSource(FieldSourceEnum.PREDIX_TIMESERIES.name());
					field.setFieldIdentifier(fieldIdentifier);
					fieldData.getField().add(field);
					PredixString predixString = new PredixString();
					predixString.setString(message);
					fieldData.setData(predixString);
					criteria.setFieldData(fieldData);
					List<PutFieldDataCriteria> list = new ArrayList<PutFieldDataCriteria>();
					list.add(criteria);
					putFieldDataRequest.setPutFieldDataCriteria(list);
				}else {
					putFieldDataRequest = mapper.fromJson(message, PutFieldDataRequest.class);
				}
				JsonParser parser = new JsonParser();
				JsonObject o = (JsonObject) parser.parse(message);
				
				String response = "{\"messageId\": " + o.get("messageId") + ",\"statusCode\": 202}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				session.getBasicRemote().sendText(response);
				List<Header> headers = new ArrayList<Header>();
				
				String[] headerNames = {"authorization","predix-zone-id"};
				System.out.println(session.getUserProperties());
				Map<String,List<String>> headerMap = (Map<String, List<String>>) session.getUserProperties().get("headers");
				
				for (String headerName:headerNames) {
					System.out.println("Header Name "+headerName);
					List<String> values = (List<String>) headerMap.get(headerName);
					headers.add(new BasicHeader(headerName, values.get(0)));
				}
				this.putFieldDataService.putData(putFieldDataRequest, null, headers);
			} else {
				session.getBasicRemote().sendText("SUCCESS"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("", e); //$NON-NLS-1$
		}
	}

	/**
	 * @param session
	 *            - session object
	 * @param closeReason
	 *            - The reason of close of session
	 */
	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		logger.info("Server: Session " + session.getId() + " closed because of " + closeReason.toString()); //$NON-NLS-1$ //$NON-NLS-2$
		clients.remove(session);
	}

	/**
	 * @param session
	 *            - current session object
	 * @param t
	 *            - Throwable instance containing error info
	 */
	@OnError
	public void onError(Session session, Throwable t) {
		logger.error("Server: Session " + session.getId() + " closed because of " + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.putFieldDataService = applicationContext.getBean(PutRouter.class);
		logger.info(".............Setting Beans.......");
	}
}
