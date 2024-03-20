package com.asrevo.cvhome.s2s.utils;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.service.invoker.HttpServiceArgumentResolver;

public class PageableSerializeParamArgumentResolver implements HttpServiceArgumentResolver {
    @Override
    public boolean resolve(Object argument, MethodParameter parameter, HttpRequestValues.Builder requestValues) {
        try {
            if (argument instanceof Pageable pageable) {
                requestValues.addRequestParameter("page", String.valueOf(pageable.getPageNumber()));
                requestValues.addRequestParameter("size", String.valueOf(pageable.getPageSize()));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;

    }
}
