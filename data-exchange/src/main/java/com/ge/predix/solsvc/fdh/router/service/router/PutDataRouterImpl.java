/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.router.service.router;

import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.filter.Filter;
import com.ge.predix.entity.model.Model;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.solsvc.fdh.handler.PutDataHandler;
import com.ge.predix.solsvc.fdh.router.validator.RouterPutDataCriteriaValidator;
import com.ge.predix.solsvc.fdh.router.validator.RouterPutDataValidator;

/**
 * 
 * @author predix
 */
@Component(value = "putFieldDataService")
public class PutDataRouterImpl implements PutRouter, ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(PutDataRouterImpl.class);

	@Autowired
	private RouterPutDataValidator validator;

	@Autowired
	private RouterPutDataCriteriaValidator criteriaValidator;

	@Autowired
	@Qualifier("assetPutFieldDataHandler")
	private PutDataHandler putFieldDataHandler;

	private ApplicationContext context;

	@Autowired
	@Qualifier("timeseriesPutFieldDataHandler")
	private PutDataHandler tsPutFieldDataHandler;

	/**
	 * @param request
	 *            -
	 * @param headers
	 *            -
	 * @return -
	 */
	@Override
	@SuppressWarnings({ "nls" })
	public PutFieldDataResult putData(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers) {
		this.validator.validate(request);

		PutFieldDataResult fullResult = new PutFieldDataResult();
		for (PutFieldDataCriteria fieldDataCriteria : request.getPutFieldDataCriteria()) {
			for (Field field : fieldDataCriteria.getFieldData().getField()) {
				try {
					if (field.getFieldIdentifier().getSource() != null && field.getFieldIdentifier().getSource().equals(FieldSourceEnum.PREDIX_ASSET.name())) {
						this.criteriaValidator.validatePutFieldDataCriteria(fieldDataCriteria);
						processSinglePredixAsset(request, modelLookupMap, headers, fullResult, fieldDataCriteria);
					} else if (field.getFieldIdentifier().getSource() != null && field.getFieldIdentifier().getSource().equals(FieldSourceEnum.PREDIX_TIMESERIES.name())) {
						processPredixTimeseries(request, modelLookupMap, headers, fullResult, fieldDataCriteria);
					} else if (field.getFieldIdentifier().getSource().contains("handler/")) {
						this.criteriaValidator.validatePutFieldDataCriteria(fieldDataCriteria);
						processSingleCustomHandler(request, modelLookupMap, headers, fullResult, fieldDataCriteria);
					} else
						throw new UnsupportedOperationException(
								"Source=" + field.getFieldIdentifier().getSource() + " not supported");
				} catch (Throwable e) {
					// if one of the FieldData can't be routed/stored we'll
					// continue with the others. The caller should submit
					// compensating transactions.
					String fieldString = null;
					if (fieldDataCriteria != null && fieldDataCriteria.getFieldData().getField() != null)
						fieldString = fieldDataCriteria.getFieldData().getField().toString();
					Filter filter = null;
					if (fieldDataCriteria != null && fieldDataCriteria.getFilter() != null)
						filter = fieldDataCriteria.getFilter();
					String msg = "unable to process request errorMsg=" + e.getMessage() + " request.correlationId="
							+ request.getCorrelationId() 
							+ " filter=" + filter + " for field=" + fieldString;
					log.error(msg);
					fullResult.getErrorEvent().add(msg);
				}
			}

		}
		return fullResult;
	}

	private void processPredixTimeseries(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers, PutFieldDataResult fullResult, PutFieldDataCriteria fieldDataCriteria) {
		{
			PutFieldDataRequest singleRequest = makeSingleRequest(request, fieldDataCriteria);
			PutFieldDataResult singleResult = this.tsPutFieldDataHandler.putData(singleRequest, modelLookupMap, headers,
					HttpMethod.POST);
			if (singleResult.getErrorEvent() != null && singleResult.getErrorEvent().size() > 0)
				fullResult.getErrorEvent().addAll(singleResult.getErrorEvent());
		}

	}

	/**
	 * @param request
	 * @param headers
	 * @param fullResult
	 * @param fieldDataCriteria
	 *            -
	 */
	@SuppressWarnings("nls")
	private void processSingleCustomHandler(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers, PutFieldDataResult fullResult, PutFieldDataCriteria fieldDataCriteria) {
		for (Field field : fieldDataCriteria.getFieldData().getField()) {

			PutFieldDataRequest singleRequest = makeSingleRequest(request, fieldDataCriteria);

			String source = field.getFieldIdentifier().getSource();
			String beanName = source.substring(source.indexOf("handler/") + 8);
			beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);

			PutDataHandler bean = (PutDataHandler) this.context.getBean(beanName);
			PutFieldDataResult singleResult = bean.putData(singleRequest, modelLookupMap, headers, HttpMethod.POST);
			if (singleResult.getErrorEvent() != null && singleResult.getErrorEvent().size() > 0)
				fullResult.getErrorEvent().addAll(singleResult.getErrorEvent());
		}
	}

	/**
	 * @param request
	 * @param headers
	 * @param fullResult
	 * @param fieldDataCriteria
	 *            -
	 */
	private void processSinglePredixAsset(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers, PutFieldDataResult fullResult, PutFieldDataCriteria fieldDataCriteria) {
		PutFieldDataRequest singleRequest = makeSingleRequest(request, fieldDataCriteria);
		PutFieldDataResult singleResult = this.putFieldDataHandler.putData(singleRequest, modelLookupMap, headers,
				HttpMethod.POST);
		if (singleResult.getErrorEvent() != null && singleResult.getErrorEvent().size() > 0)
			fullResult.getErrorEvent().addAll(singleResult.getErrorEvent());
	}

	/**
	 * @param request
	 * @param fieldDataCriteria
	 * @return
	 */
	private PutFieldDataRequest makeSingleRequest(PutFieldDataRequest request,
			PutFieldDataCriteria putFieldDataCriteria) {
		PutFieldDataRequest singleRequest = new PutFieldDataRequest();
		singleRequest.setCorrelationId(request.getCorrelationId());
		singleRequest.setExternalAttributeMap(request.getExternalAttributeMap());
		singleRequest.getPutFieldDataCriteria().add(putFieldDataCriteria);

		return singleRequest;
	}

	/**
	 * Each FieldData might have a different handler. This creates a Request
	 * with just the one FieldData for the current Field
	 * 
	 * @param fieldData
	 * @return
	 */
	@SuppressWarnings("unused")
	private PutFieldDataRequest adaptNewRequest(PutFieldDataRequest request, PutFieldDataCriteria fieldDataCriteria) {

		PutFieldDataRequest newRequest = new PutFieldDataRequest();
		newRequest.setExternalAttributeMap(request.getExternalAttributeMap());
		newRequest.setCorrelationId(request.getCorrelationId());
		newRequest.getPutFieldDataCriteria().add(fieldDataCriteria);
		return newRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.context.ApplicationContextAware#setApplicationContext
	 * (org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}

}
