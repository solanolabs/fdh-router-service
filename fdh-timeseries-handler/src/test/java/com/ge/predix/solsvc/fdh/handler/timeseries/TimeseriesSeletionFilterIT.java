package com.ge.predix.solsvc.fdh.handler.timeseries;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;

/**
 * 
 * 
 * @author 212421693
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations =
{
		"classpath*:META-INF/spring/ext-util-scan-context.xml",
        "classpath*:META-INF/spring/predix-rest-client-scan-context.xml", 
        "classpath*:META-INF/spring/predix-websocket-client-scan-context.xml",
        "classpath*:META-INF/spring/predix-rest-client-sb-properties-context.xml",
        "classpath*:META-INF/spring/timeseries-bootstrap-scan-context.xml"       
})
@IntegrationTest(
{
    "server.port=0"
})
@ComponentScan("com.ge.predix.solsvc.restclient")
@ActiveProfiles("local")
public class TimeseriesSeletionFilterIT
{

    private static Logger          log              = LoggerFactory.getLogger(TimeseriesSeletionFilterIT.class);

    
    @Autowired
    private TimeseriesFactory timeseriesFactory;

    /**
     * 
     */
    @SuppressWarnings("nls")
    @Test
    public void testInjection()
    {
    }

}
