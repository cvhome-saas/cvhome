package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.Identifier;
import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class IdentifierSerializeParamArgumentResolver implements HttpServiceArgumentResolver {
    @Override
    public boolean resolve(
            Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof Identifier identifier) {
                String name = parameter.getParameterName();
                String value = identifier.getId().toString();
                requestValues.addRequestParameter(name, value);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
