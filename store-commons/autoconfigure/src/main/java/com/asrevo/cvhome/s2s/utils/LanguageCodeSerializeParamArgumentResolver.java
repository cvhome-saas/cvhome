package com.asrevo.cvhome.s2s.utils;

import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import com.asrevo.cvhome.commons.domain.LanguageCode;

public class LanguageCodeSerializeParamArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof LanguageCode(String code)) {
                requestValues.addRequestParameter("lang", code);
                return true;
            }
        } catch (Exception _) {
            return false;
        }
        return false;
    }

}
