/*
 * Copyright (c) 2014 General Electric Company. All rights reserved.
 *
 * The copyright to the computer software herein is the property of
 * General Electric Company. The software may be used and/or copied only
 * with the written permission of General Electric Company or in accordance
 * with the terms and conditions stipulated in the agreement/contract
 * under which the software has been supplied.
 */
package com.ge.fdh.asset.validator;

import org.springframework.stereotype.Component;

import com.ge.dsp.pm.fielddatahandler.entity.putfielddata.PutFieldDataRequest;
import com.ge.dsp.pm.fielddatahandler.entity.putfielddata.PutFieldDataResult;

/**
 * validates PutFieldDataRequest
 */
@SuppressWarnings("nls")
@Component
public class PutFieldDataRequestValidator
{
    /**
     * @param putFieldDataRequest -
     * @param putFieldDataResult -
     * @param putFieldDataRequestAdapter -
     * @return -
     */
    public boolean validate(PutFieldDataRequest putFieldDataRequest, PutFieldDataResult putFieldDataResult)
    {
        if ( putFieldDataRequest == null )
        {
            putFieldDataResult.getErrorEvent().add("putFieldDataRequest is null");
            return false;
        }
        if ( !isValidSolutionId(putFieldDataRequest) )
        {
            putFieldDataResult.getErrorEvent().add("invalid solutionId");
            return false;
        }


        return true;
    }



    private boolean isValidSolutionId(PutFieldDataRequest putFieldDataRequest)
    {
        try
        {
            if ( putFieldDataRequest.getSolutionIdentifier().getId().toString() != null )
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
