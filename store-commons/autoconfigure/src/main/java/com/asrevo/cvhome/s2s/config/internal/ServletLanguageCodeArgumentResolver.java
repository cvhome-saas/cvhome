package com.asrevo.cvhome.s2s.config.internal;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.asrevo.cvhome.s2s.utils.LanguageUtils;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public class ServletLanguageCodeArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(LanguageCode.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        return Optional.ofNullable(request).map(req -> req.getParameter(LanguageUtils.LANG))
                .map(LanguageUtils::getRESTLanguageCode)
                .orElseGet(() -> LanguageUtils.getRESTLanguageCode(null));
    }

}
