package com.asrevo.cvhome.store.config;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

import static com.asrevo.cvhome.store.core.constants.Constants.DEFAULT_STORE;


@Component
public class MerchantStoreArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String REQUEST_PARAMETER_STORE = "store";
    private final StoreFacade storeFacade;

    private final AccessEvaluator accessEvaluator;

    public MerchantStoreArgumentResolver(StoreFacade storeFacade, AccessEvaluator accessEvaluator) {
        this.storeFacade = storeFacade;
        this.accessEvaluator = accessEvaluator;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(MerchantStore.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String storeCode = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_STORE))
                .filter(StringUtils::isNotBlank).orElse(DEFAULT_STORE);
        // todo get from cache

        if (parameter.hasParameterAnnotation(SecuredResource.class)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean hasAccess = accessEvaluator.hasAccessOnStoreFindOne(authentication, new ManagerStoreId(storeCode));
            if (!hasAccess) {
                throw new UnauthorizedException("Cannot authorize user for store " + storeCode);
            }
        }

        return storeFacade.get(storeCode);
    }
}
