package com.asrevo.cvhome.s2s.config.internal;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServletStoreMerchantIdArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String REQUEST_PARAMETER_STORE = "store";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(StoreMerchantId.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String storeCode = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_STORE))
                .filter(it -> !it.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Missing required parameter 'store'"));

        return new StoreMerchantId(storeCode);
    }

}
