/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.fdh.handler.asset.processor.dx;

import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ge.fdh.handler.asset.PutFieldDataHandlerImpl;
import com.ge.fdh.handler.asset.common.FieldModel;
import com.ge.fdh.handler.asset.helper.PaUtility;
import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.fielddata.Data;
import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.model.Model;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.solsvc.fdh.handler.PutDataHandler;

/**
 * PutFieldDataProcessor processes PutFieldDataRequest - Retrieves the asset per
 * the asset selector in the request - For each field data in the list, convert
 * UoM as needed and update the asset with given data
 * 
 * @author 212397779
 */
@SuppressWarnings("nls")
@Component(value = "sampleDxAssetPutFieldDataHandler")
public class SampleDxPutFieldDataHandlerImpl extends PutFieldDataHandlerImpl implements PutDataHandler {
	private static final Logger log = LoggerFactory.getLogger(SampleDxPutFieldDataHandlerImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ge.fdh.asset.processor.IPutFieldDataProcessor#processRequest(com.ge.
	 * predix.entity.putfielddata.PutFieldDataRequest)
	 */
	@Override
	public PutFieldDataResult putData(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers, String httpMethod) {
		try {
			PutFieldDataResult putFieldDataResult = new PutFieldDataResult();

			if (!this.putFieldDataRequestValidator.validate(request, putFieldDataResult))
				return putFieldDataResult;

			this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());

			putFieldDataProcessor(request, putFieldDataResult, modelLookupMap, headers, httpMethod);
			return putFieldDataResult;
		} catch (Throwable e) {
			String msg = "unable to process request errorMsg=" + e.getMessage() + " request.correlationId="
					+ request.getCorrelationId() + " request = " + request;
			log.error(msg, e);
			RuntimeException exception = new RuntimeException(msg, e);
			throw exception;
		}
	}

	@Override
	protected void putFieldDataProcessor(PutFieldDataRequest putFieldDataRequest, PutFieldDataResult putFieldDataResult,
			Map<Integer, Model> modelLookupMap, List<Header> headers, String httpMethod) {
		List<PutFieldDataCriteria> fieldCriteriaList = putFieldDataRequest.getPutFieldDataCriteria();
		for (PutFieldDataCriteria fieldDataCriteria : fieldCriteriaList) {
			this.putFieldDataCriteriaValidator.validate(fieldDataCriteria, putFieldDataResult);

			if (fieldDataCriteria.getFilter() == null) {
				// no filter, let's just post the whole asset
				Data data = fieldDataCriteria.getFieldData().getData();
				if (data == null)
					throw new UnsupportedOperationException("data of type=null not supported");
				if (data instanceof PredixString) {
					String jsonString = ((PredixString) data).getString();
					this.modelFactory.createModelFromJson(jsonString, headers);
				} else
					throw new UnsupportedOperationException("data of type=" + data + " not supported");
			} else {
				for (Field field : fieldDataCriteria.getFieldData().getField()) {
					// retrieve the entity
					String fieldId = (String) field.getFieldIdentifier().getId();
					FieldModel fieldModel = PaUtility.getFieldModel(fieldId);
					// List<Model> models = retrieveModels(fieldDataCriteria,
					// fieldModel, headers, httpMethod);
					// if ( models != null )
					// {
					// for (Model model : models)
					// {
					// processModel(fieldDataCriteria, model, fieldModel,
					// modelLookupMap, headers, httpMethod);
					// }
					// }
				}
			}
		}
	}

}
