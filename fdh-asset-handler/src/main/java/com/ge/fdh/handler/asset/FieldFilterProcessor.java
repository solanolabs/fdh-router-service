/*
 * Copyright (c) 2015 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */

package com.ge.fdh.handler.asset;

import java.util.List;

import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.fdh.handler.asset.common.FieldModel;
import com.ge.fdh.handler.asset.common.ModelQuery;
import com.ge.fdh.handler.asset.helper.PaUtility;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldidentifiervalue.FieldIdentifierValue;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.filter.FieldFilter;
import com.ge.predix.entity.model.Model;
import com.ge.predix.solsvc.ext.util.JsonMapper;

/**
 * 
 * @author predix -
 */
@Component
public class FieldFilterProcessor extends AbstractFdhRequestProcessor
{

    @Autowired
    private JsonMapper jsonMapper;

    /**
     * @param fieldDataCriteria -
     * @return -
     */
    public boolean validateFieldFilter(FieldDataCriteria fieldDataCriteria)
    {
        // no validations yet
        return true;

    }

    /**
     * @param fieldDataCriteria -
     * @param headers -
     * @return -
     */
    protected List<Model> retrieveModels(FieldDataCriteria fieldDataCriteria, List<Header> headers)
    {
        // keeping it simple, assume the first Selection holds the type of model desired
        List<FieldSelection> selections = fieldDataCriteria.getFieldSelection();
        FieldSelection firstSelection = selections.get(0);
        String field = firstSelection.getFieldIdentifier().getId().toString();
        FieldModel fieldModel = PaUtility.getFieldModel(field);

        FieldFilter filter = (FieldFilter) fieldDataCriteria.getFilter();
        List<FieldIdentifierValue> modelSelector = filter.getFieldIdentifierValue();

        ModelQuery modelQuery = getModelQuery(fieldModel.getModel(), modelSelector, headers);
        List<Model> models = this.modelFactory.getModels(modelQuery.getQuery(), fieldModel.getModelForUnMarshal(),
                headers);
        return models;
    }

}
