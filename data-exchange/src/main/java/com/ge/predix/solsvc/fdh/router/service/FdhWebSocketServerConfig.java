package com.ge.predix.solsvc.fdh.router.service;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 
 * @author predix.adoption@ge.com -
 */
@Configuration
@Profile("cloud")
public class FdhWebSocketServerConfig extends ServerEndpointConfig.Configurator implements ApplicationContextAware{
	
	/* (non-Javadoc)
	 * @see javax.websocket.server.ServerEndpointConfig.Configurator#modifyHandshake(javax.websocket.server.ServerEndpointConfig, javax.websocket.server.HandshakeRequest, javax.websocket.HandshakeResponse)
	 */
	@Override
	public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
		sec.getUserProperties().put("headers", request.getHeaders()); //$NON-NLS-1$
		super.modifyHandshake(sec, request, response);
	}
	
	@Bean
	public FdhWebSocketServerEndPoint fdhWebSocketServerEndPoint() {
		return new FdhWebSocketServerEndPoint();
	}
	/**
	 * @return -
	 */
	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	/**
     * Spring application context.
     */
    private static volatile ApplicationContext context;

    @Override
    public <T> T getEndpointInstance(Class<T> clazz) throws InstantiationException {
        return context.getBean(clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }
}
