package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.core.entity.common.Criteria;
import com.asrevo.cvhome.store.core.entity.common.CriteriaOrderBy;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

@Slf4j
public class ServiceRequestCriteriaBuilderUtils {

    /**
     * Binds request parameter values to specific request criterias
     *
     */
    public static Criteria buildRequestCriterias(
            Criteria criteria, Map<String, String> mappingFields, HttpServletRequest request)
            throws RestApiException {

        if (criteria == null)
            throw new RestApiException("A criteria class type must be instantiated");

        mappingFields
                .keySet()
                .forEach(
                        p -> {
                            try {
                                setValue(criteria, request, p, mappingFields.get(p));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
        return criteria;
    }

    private static void setValue(
            Criteria criteria, HttpServletRequest request, String parameterName, String setterValue)
            throws Exception {

        try {

            PropertyAccessor criteriaAccessor =
                    PropertyAccessorFactory.forDirectFieldAccess(criteria);

            String parameterValue = request.getParameter(parameterName);
            if (parameterValue == null) return;
            // set the property directly, bypassing the mutator (if any)
            // String setterName = "set" + WordUtils.capitalize(setterValue);
            String setterName = setterValue;
            System.out.println(
                    "Trying to do this binding "
                            + setterName
                            + "('"
                            + parameterValue
                            + "') on "
                            + criteria.getClass());
            criteriaAccessor.setPropertyValue(setterName, parameterValue);

        } catch (Exception e) {
            throw new Exception("An error occured while parameter bindding", e);
        }
    }

    /**
     * deprecated
     **/
    public static Criteria buildRequest(
            Map<String, String> mappingFields, HttpServletRequest request) {

        MerchantStoreCriteria criteria = new MerchantStoreCriteria();

        String searchParam = request.getParameter("search[value]");
        String orderColums = request.getParameter("order[0][column]");

        if (!StringUtils.isBlank(orderColums)) {
            String columnName = request.getParameter("columns[" + orderColums + "][data]");
            String overwriteField = columnName;
            if (mappingFields != null && mappingFields.get(columnName) != null) {
                overwriteField = mappingFields.get(columnName);
            }
            criteria.setCriteriaOrderByField(overwriteField);
            criteria.setOrderBy(
                    CriteriaOrderBy.valueOf(request.getParameter("order[0][dir]").toUpperCase()));
        }

        String storeName = request.getParameter("storeName");
        criteria.setName(storeName);

        String retailers = request.getParameter("retailers");
        String stores = request.getParameter("stores");

        try {
            boolean retail = Boolean.parseBoolean(retailers);
            boolean sto = Boolean.parseBoolean(stores);

            criteria.setRetailers(retail);
            criteria.setStores(sto);
        } catch (Exception e) {
            log.error("Error parsing boolean values", e);
        }

        criteria.setSearch(searchParam);

        return criteria;
    }
}
