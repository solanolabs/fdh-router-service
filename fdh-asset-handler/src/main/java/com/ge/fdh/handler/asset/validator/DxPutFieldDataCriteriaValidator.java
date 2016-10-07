/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
package com.ge.fdh.handler.asset.validator;

import org.springframework.stereotype.Component;

import com.ge.predix.entity.fielddata.PredixString;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataResult;
import com.ge.predix.entity.util.map.DataMap;

/**
 * validates PutFieldDataRequest
 */
@SuppressWarnings("nls")
@Component
public class DxPutFieldDataCriteriaValidator {
	/**
	 * @param putFieldDataCriteria
	 *            -
	 * @param putFieldDataResult
	 *            -
	 * @return -
	 */
	public boolean validate(PutFieldDataCriteria putFieldDataCriteria, PutFieldDataResult putFieldDataResult) {
		if (putFieldDataCriteria.getFieldData().getData() == null) {
			putFieldDataResult.getErrorEvent().add("invalid data, data is null");
			return false;
		} else if (putFieldDataCriteria.getFieldData().getData() instanceof PredixString
				&& !((PredixString) putFieldDataCriteria.getFieldData().getData()).getString().contains("uri")) {
			putFieldDataResult.getErrorEvent()
					.add("invalid data type, expecting a PredixString with a uri key/value pair");
			return false;
		} else if (putFieldDataCriteria.getFieldData().getData() instanceof DataMap
				&& !(((DataMap) putFieldDataCriteria.getFieldData().getData()).getMap().size() > 0)) {
			putFieldDataResult.getErrorEvent().add("invalid data type, expecting a DataMap with a uri key/value pair");
			return false;
		}
		return true;
	}

	/**
	 * @param putFieldDataCriteria
	 *            -
	 * @return true if validation passes and false otherwise
	 */
	public boolean isValidRequest(PutFieldDataCriteria putFieldDataCriteria) {

		if (!validateFilter(putFieldDataCriteria)) {
			return false;
		}

		return true;
	}

	private boolean validateFilter(PutFieldDataCriteria putFieldDataCriteria) {
		try {
			if (putFieldDataCriteria.getFilter() != null) {
				return true;
			}
		} catch (Throwable t) {
			return false;
		}
		return false;
	}

}
