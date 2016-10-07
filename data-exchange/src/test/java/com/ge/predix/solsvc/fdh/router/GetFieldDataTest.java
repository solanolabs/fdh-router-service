package com.ge.predix.solsvc.fdh.router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mimosa.osacbmv3_3.OsacbmDataType;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

import com.ge.predix.entity.asset.Asset;
import com.ge.predix.entity.asset.AssetTag;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddata.Data;
import com.ge.predix.entity.osa.filter.AnchorTimeType;
import com.ge.predix.entity.osa.filter.TimeOffsetUnits;
import com.ge.predix.entity.timeseries.datapoints.queryrequest.DatapointsQuery;
import com.ge.predix.entity.timeseries.datapoints.queryresponse.DatapointsResponse;
import com.ge.predix.entity.util.map.Map;
import com.ge.predix.solsvc.bootstrap.ams.dto.Attribute;
import com.ge.predix.solsvc.bootstrap.ams.dto.CustomModel;
import com.ge.predix.solsvc.bootstrap.ams.factories.AssetFactory;
import com.ge.predix.solsvc.fdh.router.util.StringUtil;
import com.ge.predix.solsvc.restclient.impl.RestClient;
import com.ge.predix.solsvc.timeseries.bootstrap.factories.TimeseriesFactory;


/**
 * @author tturner
 */
@SuppressWarnings(
{
        "nls"
})
@ActiveProfiles("local")
@ComponentScan(basePackages={"com.ge.predix.solsvc"})
@SpringApplicationConfiguration(classes =  GetFieldDataTest.class)
public class GetFieldDataTest extends BaseTest {
    private static final Logger          log = LoggerFactory.getLogger(GetFieldDataTest.class.getName());
    
    private static final String HTTP_PAYLOAD_JSON     = "application/json";
    private static final String CONTAINER_SERVER_PORT = "9092";


    @Autowired
    private AssetFactory assetFactory;
    @Autowired
    private TimeseriesFactory timeseriesFactory;
    @Autowired
    private RestClient restClient;
    /**
     * @throws Exception -
     */
    @Before
    public void onSetUp()
            throws Exception
    {
        //
    }

    /**
     * 
     */
    @After
    public void onTearDown()
    {
        //
    }


    /**
     * @throws JMSException -
     * @throws HttpException -
     * @throws IOException -
     * @throws JAXBException -
     */
    @Test
    public void testGet()
            throws IOException, JAXBException, HttpException
    {
        String solutionId = "1000";
        String namespace = "asset";
        String attributeName = "description";
        String fieldId = namespace + "/" + attributeName;
        String fieldName = namespace + "/" + attributeName;
        String fieldSource = FieldSourceEnum.PREDIX_ASSET.name();
        String assetId = "12345";
        String[] expectedValues = new String[]
        {
            "Tanks"
        };
		String startDef = null;
		String starTimeOffset = null;
		AnchorTimeType anchorTimeType = null;
		TimeOffsetUnits timeOffsetUnits = null;
		String endDef = null;

        Date now = new Date();
        String gmtTimeString = StringUtil.convertToDateTimeString(now);

        List<Asset> assets = new ArrayList<Asset>();
        Asset asset = new Asset();
        asset.setAssetId("12345");
        asset.setAttributes(new Map());
        Attribute attribute = new Attribute(); 
        attribute.getValue().add("value");
        asset.getAttributes().put(attributeName,attribute );
        assets.add(asset );
        Mockito.when(this.assetFactory.getAssetsByFilter(Matchers.any(), Matchers.anyListOf(Header.class))).thenReturn(assets);
       
        

        callGetFieldData(solutionId, fieldId, fieldName, fieldSource, assetId, gmtTimeString, log, expectedValues, OsacbmDataType.DA_STRING, HTTP_PAYLOAD_JSON, CONTAINER_SERVER_PORT, startDef, starTimeOffset, anchorTimeType, timeOffsetUnits, endDef);
    }

    /**
     * @throws JMSException -
     * @throws HttpException -
     * @throws IOException -
     * @throws JAXBException -
     */
    @Test
    public void testGetWithTimeFilter()
            throws IOException, JAXBException, HttpException
    {
        String solutionId = "1000";
        String namespace = "classification/tag";
        String attributeName = "MyTag1";
        String fieldId = namespace + "/" + attributeName;
        String fieldName = "/classification/MyTag1";
        String fieldSource = FieldSourceEnum.PREDIX_TIMESERIES.name();
        String assetId = "12345";
        String[] expectedValues = new String[]
        {
            "123.0",
            "124.0"
        };
        Integer quality = 3;
		String startDef = "Sat Sep 26 16:38:33 PDT 2013";
		String starTimeOffset = "1";
		AnchorTimeType anchorTimeType = AnchorTimeType.DISPATCHER;
		TimeOffsetUnits timeOffsetUnits = TimeOffsetUnits.DAYS;
		String endDef = "Sat Sep 27 16:38:33 PDT 2013";
		
        Date now = new Date();
        String gmtTimeString = StringUtil.convertToDateTimeString(now);
        
        //mock asset response
        Asset asset = new Asset();
        asset.setAssetId("12345");
        asset.setAttributes(new Map());
        Attribute attribute = new Attribute(); 
        attribute.getValue().add("value");
        asset.getAttributes().put(attributeName,attribute );
        asset.setAssetTag(new Map());
        AssetTag assetTag = new AssetTag();
        assetTag.setSourceTagId("myTimeseriesTag1");
        asset.getAssetTag().put("MyTag1", assetTag);
        Mockito.when(this.assetFactory.getAsset(Matchers.anyString(), Matchers.anyListOf(Header.class))).thenReturn(asset);
        
        //mock timeseries query response
        DatapointsResponse datapointsResponse = new DatapointsResponse();
        
        com.ge.predix.entity.timeseries.datapoints.queryresponse.Tag tag = new com.ge.predix.entity.timeseries.datapoints.queryresponse.Tag();
        List<com.ge.predix.entity.timeseries.datapoints.queryresponse.Tag> tags= new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryresponse.Tag>();
        
        com.ge.predix.entity.timeseries.datapoints.queryresponse.Results result = new com.ge.predix.entity.timeseries.datapoints.queryresponse.Results();
        List<com.ge.predix.entity.timeseries.datapoints.queryresponse.Results> results = new ArrayList<com.ge.predix.entity.timeseries.datapoints.queryresponse.Results>();
        
        List values = new ArrayList();
        List<Object> value = new ArrayList<Object>();
              
        value.add(new Double(new Date().getTime()));
        value.add(new Double(expectedValues[0]));
        value.add(quality);     
        values.add(value);
        
        value = new ArrayList<Object>();
        value.add(new Double(new Date().getTime()));
        value.add(new Double(expectedValues[1]));
        value.add(quality);
        values.add(value);
        
        result.setValues(values);
        results.add(result);
        
        tag.setResults(results);
        tags.add(tag);
        
        datapointsResponse.setTags(tags);
        
        Mockito.when(this.timeseriesFactory.queryForDatapoints(Matchers.anyString(), Matchers.any(DatapointsQuery.class), Matchers.anyListOf(Header.class))).thenReturn(datapointsResponse);
        
        List<Header> headers = new ArrayList<Header>();
        headers.add(new BasicHeader("Authorization", null));
        Mockito.when(this.restClient.getSecureToken(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(),
                Matchers.anyString())).thenReturn(headers );
        callGetFieldData(solutionId, fieldId, fieldName, fieldSource, assetId, gmtTimeString, log, expectedValues, OsacbmDataType.DM_DATA_SEQ, HTTP_PAYLOAD_JSON, CONTAINER_SERVER_PORT, startDef, starTimeOffset, anchorTimeType, timeOffsetUnits, endDef);

    }



}
