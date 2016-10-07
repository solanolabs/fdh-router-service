package com.ge.predix.solsvc.fdh.router.service.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ge.predix.entity.field.fieldidentifier.FieldSourceEnum;
import com.ge.predix.entity.fielddatacriteria.FieldDataCriteria;
import com.ge.predix.entity.fieldselection.FieldSelection;
import com.ge.predix.entity.getfielddata.GetFieldDataRequest;
import com.ge.predix.entity.getfielddata.GetFieldDataResult;
import com.ge.predix.entity.model.Model;
import com.ge.predix.solsvc.fdh.handler.GetDataHandler;
import com.ge.predix.solsvc.fdh.router.validator.RouterGetDataValidator;

/**
 * 
 * @author predix -
 */
@Component(value = "getFieldDataService")
public class GetDataRouterImpl
        implements GetRouter, ApplicationContextAware
{

    @Autowired
    private RouterGetDataValidator        validator;

    @Autowired
    @Qualifier("assetGetFieldDataHandler")
    private GetDataHandler           getDataHandler;


    private ApplicationContext context;

    /**
     * This methods loops through the Requests for a given asset - and continues
     * through the same processing logic for a different asset. We need to start
     * transaction at this point here to ensure no lazy initialization exception
     * is thrown due to restoring the FieldMetaData objects.
     * 
     * @param getFieldDataRequest
     *            -
     * @return -
     */
    @Override
    public GetFieldDataResult getData(GetFieldDataRequest getFieldDataRequest, Map<Integer, Model> modelLookupMap, List<Header> headers)
    {
        this.validator.validate(getFieldDataRequest);
        GetFieldDataResult fullResult = processRequest(getFieldDataRequest, modelLookupMap, headers);
        return fullResult;
    }

    /**
     */
    @SuppressWarnings("nls")
    private GetFieldDataResult processRequest(GetFieldDataRequest request,Map<Integer, Model> modelLookupMap, List<Header> headers)
    {
        try
        {
            GetFieldDataResult fullResult = new GetFieldDataResult();
            for (FieldDataCriteria fieldDataCriteria : request.getFieldDataCriteria())
            {
                for (FieldSelection fieldSelection : fieldDataCriteria.getFieldSelection())
                {
                	if ( fieldSelection.getFieldIdentifier().getSource() == null )
                        throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier().getSource()=null not supported" );
                    String sourceName = fieldSelection.getFieldIdentifier().getSource();
                    if ( sourceName == null )
                        throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier().getSource()=" + sourceName + " not supported" );

					if ( sourceName.equals(FieldSourceEnum.PREDIX_ASSET.name()) )
                    {
                        processSinglePredixAsset(request, fullResult, fieldDataCriteria, fieldSelection, modelLookupMap, headers);
                    }
                    else if ( sourceName.equals(FieldSourceEnum.PREDIX_TIMESERIES.name()) )
                    {
                    	fieldSelection.getFieldIdentifier().setSource("handler/timeseriesGetDataHandler");
                    	processSingleCustomHandler(request, headers, fullResult, fieldDataCriteria, fieldSelection);
                    }
                    else if ( sourceName.contains("/handler")){
                        processSingleCustomHandler(request, headers, fullResult, fieldDataCriteria, fieldSelection);
                    }
                    else
                        throw new UnsupportedOperationException("fieldSelection.getField().getFieldIdentifier().getSource()=" + sourceName + " not supported" );

                }
            }
            return fullResult;
        }
        catch (IllegalStateException e)
        {
            throw new RuntimeException("error in getFieldData", e);
        }
    }

    /**
     * @param request
     * @param headers
     * @param fullResult
     * @param fieldDataCriteria
     * @param fieldSelection -
     */
    @SuppressWarnings("nls")
    private void processSingleCustomHandler(GetFieldDataRequest request, List<Header> headers, GetFieldDataResult fullResult,
            FieldDataCriteria fieldDataCriteria, FieldSelection fieldSelection)
    {
        GetFieldDataRequest singleRequest = makeSingleRequest(request, fieldDataCriteria, fieldSelection);
        
        String source = fieldSelection.getFieldIdentifier().getSource();
        if ( !source.contains("handler/"))
            throw new UnsupportedOperationException("please add the name of Spring Bean after handler  e.g. handler/sampleHandler");
        String beanName = source.substring(source.indexOf("handler/")+8);
        beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
        
        GetDataHandler bean = (GetDataHandler) this.context.getBean(beanName);
        Map<Integer, Model> modelLookupMap = new HashMap<Integer, Model>();

        GetFieldDataResult singleResult = bean.getData(singleRequest,modelLookupMap, headers);
        fullResult.getFieldData().addAll(singleResult.getFieldData());
    }


    /**
     * @param request
     * @param fieldDataCriteria
     * @param fieldSelection
     * @param modelLookupMap 
     * @param context
     * @param result
     */
    private void processSinglePredixAsset(GetFieldDataRequest request, GetFieldDataResult fullResult, FieldDataCriteria fieldDataCriteria, FieldSelection fieldSelection,
            Map<Integer, Model> modelLookupMap, List<Header> headers)
    {
        GetFieldDataRequest singleRequest = makeSingleRequest(request, fieldDataCriteria, fieldSelection);
        GetFieldDataResult singleResult = this.getDataHandler.getData(singleRequest, modelLookupMap, headers);
        fullResult.getFieldData().addAll(singleResult.getFieldData());
    }

    private GetFieldDataRequest makeSingleRequest(GetFieldDataRequest request, FieldDataCriteria fieldDataCriteria,
            FieldSelection fieldSelection)
    {
        GetFieldDataRequest singleRequest = new GetFieldDataRequest();
        singleRequest.setCorrelationId(request.getCorrelationId());
        singleRequest.setExternalAttributeMap(request.getExternalAttributeMap());
        singleRequest.setSolutionIdentifier(request.getSolutionIdentifier());
        FieldDataCriteria singleFieldDataCriteria = new FieldDataCriteria();
        singleFieldDataCriteria.setResultId(fieldDataCriteria.getResultId());
        singleFieldDataCriteria.getFieldSelection().add(fieldSelection);
        singleFieldDataCriteria.setFilter(fieldDataCriteria.getFilter());
        singleRequest.getFieldDataCriteria().add(singleFieldDataCriteria);
        return singleRequest;
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.context = applicationContext;
    }

}
