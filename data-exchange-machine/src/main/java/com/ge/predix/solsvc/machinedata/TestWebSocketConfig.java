package com.ge.predix.solsvc.machinedata;


/**
 * 
 * @author 212438846
 * Class TestWebSocketConfig1 implements IWebSocketConfig. Every separate websocket server connection requires the
 * IWebSocketConfig interface to be implemented per connection.   
 */

public class TestWebSocketConfig{

    public void setWsProxyHost(String wsProxyHost) {
		this.wsProxyHost = wsProxyHost;
	}



	public void setWsProxyPort(String wsProxyPort) {
		this.wsProxyPort = wsProxyPort;
	}



	public void setWsUri(String wsUri) {
		this.wsUri = wsUri;
	}



	public void setZoneId(String zoneId) {
		this.zoneId = zoneId;
	}



	public void setWsMaxIdle(int wsMaxIdle) {
		this.wsMaxIdle = wsMaxIdle;
	}



	public void setWsMaxActive(int wsMaxActive) {
		this.wsMaxActive = wsMaxActive;
	}



	public void setWsMaxWait(int wsMaxWait) {
		this.wsMaxWait = wsMaxWait;
	}


	private String wsProxyHost;

    private String wsProxyPort;

    private String wsUri;

    private String zoneId;

    private int    wsMaxIdle;

    private int    wsMaxActive;

    private int    wsMaxWait;

    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxIdle()
     */
    public int getWsMaxIdle()
    {
        return this.wsMaxIdle;
    }



    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxActive()
     */
    public int getWsMaxActive()
    {
        return this.wsMaxActive;
    }


    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsMaxWait()
     */
    public int getWsMaxWait()
    {
        return this.wsMaxWait;
    }


    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsProxyHost()
     */
    public String getWsProxyHost()
    {
        return this.wsProxyHost;
    }


    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsProxyPort()
     */
    public String getWsProxyPort()
    {
        return this.wsProxyPort;
    }


    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getWsUri()
     */
    public String getWsUri()
    {
        return this.wsUri;
    }


    /*
     * (non-Javadoc)
     * @see com.ge.predix.solsvc.websocket.config.IWebSocketConfig#getZoneId()
     */
    public String getZoneId()
    {
        return this.zoneId;
    }

}
