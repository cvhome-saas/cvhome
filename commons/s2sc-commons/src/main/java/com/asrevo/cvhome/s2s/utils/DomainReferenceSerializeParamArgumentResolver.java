package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.DomainReference;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class DomainReferenceSerializeParamArgumentResolver implements HttpServiceArgumentResolver {
    @Override
    public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof DomainReference domainReference) {
                requestValues.addRequestParameter("reference", domainReference.reference());
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;

    }
}
