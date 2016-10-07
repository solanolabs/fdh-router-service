/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.handler.timeseries;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ge.predix.entity.model.Model;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.entity.util.map.AttributeMap;
import com.ge.predix.entity.util.map.Entry;
import com.ge.predix.solsvc.fdh.handler.PutDataHandler;

/**
 * PutFieldDataProcessor processes PutFieldDataRequest - Puts data in the time
 * series handlers -
 * 
 * @author predix
 */
@SuppressWarnings("nls")
@Component(value = "timeseriesPutFieldDataHandler")
public class TimeseriesPutDataHandler implements PutDataHandler {
	private static final Logger log = LoggerFactory.getLogger(TimeseriesPutDataHandler.class);

	@Autowired
	@Qualifier("timeSeriesPutExecutor")
	private TimeSeriesPutExecutor timeSeriesPutExecutor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ge.fdh.asset.processor.IPutFieldDataProcessor#processRequest(com.
	 * ge.dsp.pm.fielddatahandler.entity.putfielddata.PutFieldDataRequest)
	 */
	@Override
	public PutFieldDataResult putData(PutFieldDataRequest request, Map<Integer, Model> modelLookupMap,
			List<Header> headers, String httpMethod) {
		try {
			UUID idOne = UUID.randomUUID();
			PutFieldDataResult putFieldDataResult = getPutFieldDataResult(idOne);
			this.timeSeriesPutExecutor.putFieldDataProcessor(request, putFieldDataResult, headers);
			return putFieldDataResult;
		} catch (Throwable e) {
			String msg = "unable to process request errorMsg=" + e.getMessage() + " request.correlationId="
					+ request.getCorrelationId() + " request = " + request;
			log.error(msg, e);
			RuntimeException dspPmException = new RuntimeException(msg, e);
			throw dspPmException;
		}
	}

	/**
	 * @param idOne
	 * @return
	 */
	private PutFieldDataResult getPutFieldDataResult(UUID idOne) {
		PutFieldDataResult putFieldDataResult = new PutFieldDataResult();
		AttributeMap attributeMap = new AttributeMap();
		Entry uuidEntru = new Entry();
		uuidEntru.setKey("UUID");
		uuidEntru.setValue(idOne.toString());
		attributeMap.getEntry().add(uuidEntru);
		putFieldDataResult.setExternalAttributeMap(attributeMap);
		return putFieldDataResult;
	}

}
