package com.ge.predix.solsvc.fdh.router.util;

import org.mimosa.osacbmv3_3.DMBool;
import org.mimosa.osacbmv3_3.DMReal;

import com.ge.predix.entity.asset.Asset;
import com.ge.predix.entity.field.fieldidentifier.FieldIdentifier;
import com.ge.predix.entity.fielddata.FieldData;
import com.ge.predix.entity.fielddata.OsaData;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldidentifiervalue.FieldIdentifierValue;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.filter.FieldFilter;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.solution.identifier.solutionidentifier.SolutionIdentifier;
import com.ge.predix.entity.util.map.DataMap;
import com.ge.predix.solsvc.ext.util.FieldUtil;
import com.ge.predix.solsvc.ext.util.JsonMapper;

/**
 * 
 * @author predix
 */
public class TestData {

	/**
	 * @param field
	 *            -
	 * @param fieldSource
	 *            -
	 * @param expectedDataType
	 *            -
	 * @param uriField
	 *            -
	 * @param uriFieldValue
	 *            -
	 * @param startTime
	 *            -
	 * @param endTime
	 *            -
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static GetFieldDataRequest getFieldDataRequest(String field,
			String fieldSource, String expectedDataType, Object uriField,
			Object uriFieldValue, Object startTime, Object endTime) {
		GetFieldDataRequest getFieldDataRequest = new GetFieldDataRequest();
		SolutionIdentifier solutionIdentifier = new SolutionIdentifier();
		solutionIdentifier.setId(1001);
		getFieldDataRequest.setSolutionIdentifier(solutionIdentifier);

		FieldDataCriteria fieldDataCriteria = new FieldDataCriteria();

		FieldFilter fieldFilter = new FieldFilter();
		FieldSelection fieldSelection = new FieldSelection();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId(field);
		fieldIdentifier.setSource(fieldSource );
		fieldSelection.setFieldIdentifier(fieldIdentifier);
		fieldSelection.setExpectedDataType(expectedDataType);
		fieldDataCriteria.getFieldSelection().add(fieldSelection);
		fieldDataCriteria.setFilter(fieldFilter);

		// add FieldIdValue pair for assetId
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId(uriField);
		// assetIdFieldIdentifier.setSource(FieldSourceEnum.PREDIX_ASSET.name());
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue(uriFieldValue);
		fieldFilter.getFieldIdentifierValue().add(fieldIdentifierValue);

		if (startTime != null && endTime != null) {
			// add FieldIdValue pair for time
			FieldIdentifierValue startTimefieldIdentifierValue = new FieldIdentifierValue();
			FieldIdentifier startTimeFieldIdentifier = new FieldIdentifier();
			startTimeFieldIdentifier.setId("startTime");
			startTimefieldIdentifierValue
					.setFieldIdentifier(startTimeFieldIdentifier);
			// fieldIdentifierValue.setValue("1438906239475");
			startTimefieldIdentifierValue.setValue(startTime);
			fieldFilter.getFieldIdentifierValue().add(
					startTimefieldIdentifierValue);

			FieldIdentifierValue endTimefieldIdentifierValue = new FieldIdentifierValue();
			FieldIdentifier endTimeFieldIdentifier = new FieldIdentifier();
			endTimeFieldIdentifier.setId("endTime");
			endTimefieldIdentifierValue
					.setFieldIdentifier(endTimeFieldIdentifier);
			// fieldIdentifierValue.setValue("1438906239475");
			endTimefieldIdentifierValue.setValue(endTime);
			fieldFilter.getFieldIdentifierValue().add(
					endTimefieldIdentifierValue);
		}

		getFieldDataRequest.getFieldDataCriteria().add(fieldDataCriteria);
		return getFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest putFieldDataRequestSetAlertStatus() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Asset to Query
		FieldFilter filter = new FieldFilter();
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId("/asset/assetId");
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue("/asset/compressor-2015");
		filter.getFieldIdentifierValue().add(fieldIdentifierValue);

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier
				.setId("/asset/assetTag/crank-frame-dischargepressure/tagDatasource/tagExtensions/attributes/alertStatus/value");
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		OsaData crankFrameVelocityData = new OsaData();
		DMBool crankFrameVelocity = new DMBool();
		crankFrameVelocity.setValue(true);
		crankFrameVelocityData.setDataEvent(crankFrameVelocity);
		fieldData.getField().add(field);
		fieldData.setData(crankFrameVelocityData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		fieldDataCriteria.setFilter(filter);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @return -
	 */
	@SuppressWarnings("nls")
	public static PutFieldDataRequest putFieldDataRequest() {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Asset to Query
		FieldFilter filter = new FieldFilter();
		FieldIdentifierValue fieldIdentifierValue = new FieldIdentifierValue();
		FieldIdentifier assetIdFieldIdentifier = new FieldIdentifier();
		assetIdFieldIdentifier.setId("/asset/assetId");
		fieldIdentifierValue.setFieldIdentifier(assetIdFieldIdentifier);
		fieldIdentifierValue.setValue("/asset/compressor-2015");
		filter.getFieldIdentifierValue().add(fieldIdentifierValue);

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier
				.setId("/asset/assetTag/crank-frame-velocity/outputMaximum");
		fieldIdentifier.setSource("PREDIX_ASSET");
		field.setFieldIdentifier(fieldIdentifier);
		OsaData crankFrameVelocityData = new OsaData();
		DMReal crankFrameVelocity = new DMReal();
		crankFrameVelocity.setValue(19.88);
		crankFrameVelocityData.setDataEvent(crankFrameVelocity);
		fieldData.getField().add(field);
		fieldData.setData(crankFrameVelocityData);

		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		fieldDataCriteria.setFilter(filter);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

	/**
	 * @param dxUri
	 *            -
	 * @param jsonMapper
	 *            -
	 * @return -
	 */
	@SuppressWarnings({ "nls" })
	public static PutFieldDataRequest dxPutFieldDataRequest(String dxUri,
			JsonMapper jsonMapper) {
		PutFieldDataRequest putFieldDataRequest = new PutFieldDataRequest();

		// Data to change
		FieldData fieldData = new FieldData();
		com.ge.predix.entity.field.Field field = new com.ge.predix.entity.field.Field();
		FieldIdentifier fieldIdentifier = new FieldIdentifier();
		fieldIdentifier.setId(dxUri);
		fieldIdentifier.setSource("handler/dxAssetPutFieldDataHandler");
		field.setFieldIdentifier(fieldIdentifier);

		fieldData.getField().add(field);
		Asset asset = new Asset();
		asset.setUri("/asset/1");
		asset.setDescription("an asset");
		String json = jsonMapper.toJson(asset);
		DataMap data = FieldUtil.toDataMap(json, jsonMapper);

		fieldData.setData(data);
		PutFieldDataCriteria fieldDataCriteria = new PutFieldDataCriteria();
		fieldDataCriteria.setFieldData(fieldData);
		putFieldDataRequest.getPutFieldDataCriteria().add(fieldDataCriteria);

		return putFieldDataRequest;
	}

}
