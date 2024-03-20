package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.Domain;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

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
