package com.ge.predix.solsvc.fdh.router.it;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mimosa.osacbmv3_3.OsacbmDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ge.dsp.pm.ext.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.dsp.pm.ext.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.dsp.pm.fielddatahandler.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.solsvc.ext.util.JsonMapper;
import com.ge.predix.solsvc.fdh.router.boot.FdhRouterApplication;
import com.ge.predix.solsvc.fdh.router.util.TestData;
import com.ge.predix.solsvc.restclient.config.OauthRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author predix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FdhRouterApplication.class)
@WebAppConfiguration
@IntegrationTest({"server.port=9092"})
public class CustomHandlerGetFieldDataIT
{

    private static final Logger     log = LoggerFactory.getLogger(CustomHandlerGetFieldDataIT.class);

    @Autowired
    private RestClient                    restClient;
    
    @Autowired
    private OauthRestConfig  restConfig;

    @Autowired
    private JsonMapper jsonMapper;

    /**
     * @throws Exception -
     */
    @BeforeClass
    public static void setUpBeforeClass()
            throws Exception
    {
        //new RestTemplate();
    }

    /**
     * @throws Exception -
     */
    @AfterClass
    public static void tearDownAfterClass()
            throws Exception
    {
        //
    }

    /**
     * @throws Exception -
     */
    @SuppressWarnings(
    {
            "nls", "unused"
    })
    @Before
    public void setUp()
            throws Exception
    {
        //
    }

    /**
     * @throws Exception -
     */
    @After
    public void tearDown()
            throws Exception
    {
        //
    }

    
    /**
     * @throws IOException -
     * @throws IllegalStateException - 
     */
    @SuppressWarnings("nls")
    @Test
    public void testGetFieldData()
            throws IllegalStateException, IOException
    {
        log.debug("================================");
        String field = "/meter/crank-frame-velocity";
        String fieldSource = FieldSourceEnum.PREDIX_TIMESERIES.name();
        String expectedDataType = OsacbmDataType.DM_DATA_SEQ.value();
        String uriField = "/asset/assetId";
        String uriFieldValue = "/asset/compressor-2015";
        String startTime = "2015-08-01 11:00:00";
        String endTime = "2015-08-08 11:00:00";
        GetFieldDataRequest request = TestData.getFieldDataRequest(field,fieldSource,expectedDataType,uriField,uriFieldValue,startTime,endTime);
        FieldIdentifier fieldIdentifier = request.getFieldDataCriteria().get(0).getFieldSelection().get(0).getFieldIdentifier();
        fieldIdentifier.setSource("/handler/sampleHandler");
        
        String url = "http://localhost:" + "9092" + "/services/fdhrouter/fielddatahandler/getfielddata";
        log.debug("URL = " + url);

        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        this.restClient.addSecureTokenForHeaders(headers, this.restConfig.getOauthClientId(), this.restConfig.getOauthClientIdEncode());
        log.debug("REQUEST: Input json to get field data = " + this.jsonMapper.toJson(request));
        
        HttpResponse response = this.restClient.post(url, this.jsonMapper.toJson(request), headers);
        
        log.debug("RESPONSE: Response from Get Field Data  = " + response);

        String responseAsString = this.restClient.getResponse(response);
        log.debug("RESPONSE: Response from Get Field Data  = " + responseAsString);

        Assert.assertNotNull(response);
        Assert.assertNotNull(responseAsString);
        Assert.assertTrue(responseAsString.contains("SampleHandler"));
        
        /*String reply = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        Assert.assertTrue(reply, response.toString().contains("HTTP/1.1 200 OK"));*/
    }
    
}
