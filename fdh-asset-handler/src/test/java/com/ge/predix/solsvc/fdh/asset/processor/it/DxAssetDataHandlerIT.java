/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.asset.processor.it;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.ws.rs.HttpMethod;

import org.apache.http.Header;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ge.fdh.handler.asset.DxGetFieldDataHandlerImpl;
import com.ge.fdh.handler.asset.DxPutFieldDataHandlerImpl;
import com.ge.fdh.handler.asset.common.ModelQuery;
import com.ge.predix.entity.asset.Asset;
import com.ge.predix.entity.dxasset.DxAsset;
import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.model.Model;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.entity.solution.identifier.solutionidentifier.SolutionIdentifier;
import com.ge.predix.solsvc.bootstrap.ams.common.AssetRestConfig;
import com.ge.predix.solsvc.bootstrap.ams.factories.ModelFactory;
import com.ge.predix.solsvc.fdh.asset.processor.AbstractRequestProcessorTest;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 * 
 * @author 212369540
 */
@SuppressWarnings({ "nls" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/ext-util-scan-context.xml",
		"classpath*:META-INF/spring/predix-rest-client-scan-context.xml",
		"classpath*:META-INF/spring/predix-rest-client-sb-properties-context.xml",
		"classpath*:META-INF/spring/fdh-asset-handler-scan-context.xml",
		"classpath*:META-INF/spring/asset-bootstrap-client-scan-context.xml"

})
public class DxAssetDataHandlerIT extends AbstractRequestProcessorTest {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(DxAssetDataHandlerIT.class);

	/**
	 * Processor for GetFieldData request
	 */
	@Autowired
	@Qualifier(value = "dxAssetGetFieldDataHandler")
	private DxGetFieldDataHandlerImpl getFieldDataProcessor;
	@Autowired
	@Qualifier(value = "dxAssetPutFieldDataHandler")
	private DxPutFieldDataHandlerImpl putFieldDataProcessor;
	@Autowired
	private RestClient restClient;
	/**
	 * 
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Autowired
	private AssetRestConfig assetRestConfig;
	@Autowired
	private ModelFactory modelFactory;

	/**
	 * @throws java.lang.Exception
	 *             -
	 */
	@Before
	public void setUp() throws Exception {
		//
	}

	/**
	 * @throws java.lang.Exception
	 *             -
	 */
	@After
	public void tearDown() throws Exception {
		//
	}

	/**
	 * - Note SampleEngine extends Model extends Data which has a JsonTypeInfo
	 * annotation so it gets polymorphically (Animal/Cat/Dog) generated {
	 * "complexType": "SampleEngine", "additionalAttributes": [ { "name":
	 * "averageSpeed", "attribute": { "value": [ "22.5" ] } } ], "averageSpeed":
	 * "22.2" }
	 * 
	 * @throws InterruptedException
	 *             -
	 */
	@Test
	public void testPostingAClassThatExtendsModel()
			throws InterruptedException {
		String uri = "/asset/engine22";
		String dxUri = "/dxAsset/1";

		List<Header> headers = this.restClient.getSecureTokenForClientId();
		this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());

		// get rid of it
		this.modelFactory.deleteModel(dxUri, headers);// $NON-NLS-1$

		// add it back
		PutFieldDataRequest putFieldDataRequest = createPutRequest(dxUri, uri);
		@SuppressWarnings("unused")
		String json = this.jsonMapper.toJson(putFieldDataRequest);

		java.util.Map<Integer, Model> modelLookupMap = new HashMap<Integer, Model>();
		// this.thrown.expect(RuntimeException.class);
		PutFieldDataResult result = this.putFieldDataProcessor.putData(putFieldDataRequest, modelLookupMap, headers, HttpMethod.POST);
        Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrorEvent().size()==0);

		ModelQuery modelQuery = new ModelQuery();
		modelQuery.setUri(dxUri);
		List<Model> resultingModelList = this.modelFactory.getModels(modelQuery.getQuery(), "DxAsset", headers);

		Assert.assertNotNull(resultingModelList);
		Assert.assertTrue(resultingModelList.size() > 0);
		DxAsset dxAsset = (DxAsset) resultingModelList.get(0);
		Assert.assertEquals(uri, ((LinkedHashMap<?,?>) dxAsset.getAsset().get("asset")).get("uri"));
	}

	/**
	 * -
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNoModelForPostOnClassThatDoesNotExist() {
		String model = "DoesNotExist";
		String uri = "/" + model + "/engine22";
		String dxUri = "/dxAsset/1";

		List<Header> headers = this.restClient.getSecureTokenForClientId();
		this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());

		// get rid of it
		this.modelFactory.deleteModel(uri, headers);// $NON-NLS-1$

		// add it back
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		putFieldDataRequest.setCorrelationId("string");

		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId(dxUri);
		field.setFieldIdentifier(fieldIdentifier);
		fieldData.getField().add(field);
		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		LinkedHashMap map = new LinkedHashMap();
		map.put("uri", uri);
		map.put("anotherAttribute","hello");
		PredixString predixString = new PredixString();
		String assetJson = this.jsonMapper.toJson(map);
		predixString.setString(assetJson);
		fieldData.setData(predixString);
		fieldDataCriteria.setFieldData(fieldData);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);
		
		@SuppressWarnings("unused")
		String json = this.jsonMapper.toJson(putFieldDataRequest);

		java.util.Map<Integer, Model> modelLookupMap = new HashMap<Integer, Model>();
		// this.thrown.expect(RuntimeException.class);
		PutFieldDataResult result = this.putFieldDataProcessor.putData(putFieldDataRequest, modelLookupMap, headers, HttpMethod.POST);
        Assert.assertNotNull(result);
		Assert.assertTrue(result.getErrorEvent().size()==0);
		
		ModelQuery modelQuery = new ModelQuery();
		modelQuery.setUri(dxUri);
		List<Model> resultingModelList = this.modelFactory.getModels(modelQuery.getQuery(), "DxAsset", headers);

		Assert.assertNotNull(resultingModelList);
		Assert.assertTrue(resultingModelList.size() > 0);
		DxAsset dxAsset = (DxAsset) resultingModelList.get(0);
		Assert.assertEquals(uri, ((java.util.Map) dxAsset.getAsset().get("asset")).get("uri"));
	}


	/**
	 * @param fieldId
	 * @param filterFieldId
	 * @param filterFieldValue
	 * @return -
	 */
	private PutFieldDataRequest createPutRequest(String fieldId, String assetUri) {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();
		putFieldDataRequest.setCorrelationId("string");

		FieldData fieldData = new FieldData();
		Field field = new Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId(fieldId);
		field.setFieldIdentifier(fieldIdentifier);
		fieldData.getField().add(field);
		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		Asset asset = new Asset();
		asset.setUri(assetUri);
		PredixString predixString = new PredixString();
		String assetJson = this.jsonMapper.toJson(asset);
		predixString.setString(assetJson);
		fieldData.setData(predixString);
		fieldDataCriteria.setFieldData(fieldData);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);
		return putFieldDataRequest;
	}

}
