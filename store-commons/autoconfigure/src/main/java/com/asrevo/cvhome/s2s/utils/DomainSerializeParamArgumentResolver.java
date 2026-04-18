package com.asrevo.cvhome.s2s.utils;

import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import com.asrevo.cvhome.commons.domain.Domain;

public class DomainSerializeParamArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof Domain domain) {
                requestValues.addRequestParameter("domain", domain.domain());
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
