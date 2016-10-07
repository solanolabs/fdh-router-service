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

import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;

/**
 * 
 * @author 212369540
 */
@Component
public class GetFieldDataValidator
{

    /**
     * @param getFieldDataRequest -
     * @param getFieldDataResult -
     * @return -
     */
    @SuppressWarnings("nls")
    public boolean validate(GetFieldDataRequest getFieldDataRequest, GetFieldDataResult getFieldDataResult)
    {
        if ( getFieldDataRequest == null )
        {
            getFieldDataResult.getErrorEvent().add("Invalid getFieldDataRequest=null");
            return false;
        }

        if ( !validateSolutionId(getFieldDataRequest) )
        {
            getFieldDataResult.getErrorEvent().add("Invalid SolutionId=");
            return false;
        }
        return true;
    }

    /**
     * @param getFieldDataRequest -
     * @return -
     */
    public boolean validateSolutionId(GetFieldDataRequest getFieldDataRequest)
    {
        try
        {
            if ( getFieldDataRequest.getSolutionIdentifier().getId() != null )
            {
                return true;
            }
        }
        catch (Throwable t)
        {
            return false;
        }
        return false;
    }

}
