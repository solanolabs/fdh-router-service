/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.predix.solsvc.fdh.router.validator;

import org.springframework.stereotype.Component;

import com.ge.predix.entity.field.Field;
import com.ge.predix.entity.filter.FieldFilter;
import com.ge.predix.entity.putfielddata.PutFieldDataCriteria;
import com.ge.predix.entity.putfielddata.PutFieldDataRequest;

/**
 * 
 * @author predix
 */
@Component
@SuppressWarnings("nls")
public class RouterPutDataCriteriaValidator extends BaseValidator {

	/**
	 * @param fieldDataCriteria
	 *            -
	 */
	public void validatePutFieldDataCriteria(PutFieldDataCriteria fieldDataCriteria) {
		for (Field field : fieldDataCriteria.getFieldData().getField()) {
			if (fieldDataCriteria.getFieldData().getField() == null)
				throw new UnsupportedOperationException(
						"fieldSelection.getField()=" + fieldDataCriteria.getFieldData().getField() + " not supported");

			if (field.getFieldIdentifier() == null)
				throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier()="
						+ field.getFieldIdentifier() + " not supported");

			if (field.getFieldIdentifier().getSource() == null)
				throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier().getSource()=null not supported");

			if (field.getFieldIdentifier().getSource() == null)
				throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier().getSource()="
						+ field.getFieldIdentifier().getSource() + " not supported");

			// if ( isFilterNull(fieldDataCriteria) )
			// throw new RuntimeException(("PutFieldDataRequest is invalid
			// isFilterNull"));

			if (isGetAssetNull(fieldDataCriteria))
				throw new RuntimeException(("PutFieldDataRequest is invalid isGetAssetNull"));

			if (isSerialNumberNull(fieldDataCriteria))
				throw new RuntimeException(("PutFieldDataRequest is invalid isSerialNumberNull"));
		}

	}

	/**
	 * @param putFieldDataRequest
	 *            -
	 * @return -
	 */
	boolean isPutFieldDataCriteriaNull(PutFieldDataRequest putFieldDataRequest) {
		if ((putFieldDataRequest.getPutFieldDataCriteria() == null
				|| putFieldDataRequest.getPutFieldDataCriteria().size() == 0)) {
			return true;
		}
		return false;
	}

	/**
	 * @param putFieldDataCriteria
	 *            -
	 * @return -
	 */
	boolean isFilterNull(PutFieldDataCriteria putFieldDataCriteria) {
		if (putFieldDataCriteria.getFilter() == null) {
			return true;
		}
		return false;
	}

	/**
	 * @param putFieldDataCriteria
	 *            -
	 * @return -
	 */
	boolean isGetAssetNull(PutFieldDataCriteria putFieldDataCriteria) {
		if (putFieldDataCriteria.getFilter() != null
				&& ((FieldFilter) putFieldDataCriteria.getFilter()).getFieldIdentifierValue().size() == 0
				&& !((FieldFilter) putFieldDataCriteria.getFilter()).getFieldIdentifierValue().get(0)
						.getFieldIdentifier().getId().toString().contains("assetId")) {
			return true;
		}
		return false;
	}

	/**
	 * @param putFieldDataCriteria
	 *            -
	 * @return -
	 */
	boolean isSerialNumberNull(PutFieldDataCriteria putFieldDataCriteria) {
		if (putFieldDataCriteria.getFilter() != null
				&& ((FieldFilter) putFieldDataCriteria.getFilter()).getFieldIdentifierValue().size() == 0
				&& ((FieldFilter) putFieldDataCriteria.getFilter()).getFieldIdentifierValue().get(0)
						.getValue() == null) {
			return true;
		}
		return false;
	}

}
