package com.asrevo.cvhome.s2s.utils;

import org.springframework.core.MethodParameter;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public class LanguageCodeSerializeParamArgumentResolver implements HttpServiceArgumentResolver {

    @Override
    public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof LanguageCode languageCode) {
                requestValues.addRequestParameter("lang", languageCode.code());
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
