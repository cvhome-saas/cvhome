package com.asrevo.cvhome.checkout.config;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;

/**
 * Turns a shopper JWT into a {@link ShopperId}: the {@code sub} claim, which cua sets to the account id. Any other
 * principal — staff, a service, nobody — resolves to {@code null}, so a handler that allows guests reads one parameter
 * either way.
 */
public class ShopperArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentShopper.class)
                && ShopperId.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return resolve(SecurityContextHolder.getContext().getAuthentication());
    }

    static ShopperId resolve(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !SecurityUtils.hasStoreCustomerRole(jwt)) {
            return null;
        }
        String sub = jwt.getToken().getSubject();
        return sub == null || sub.isBlank() ? null : new ShopperId(sub);
    }
}
