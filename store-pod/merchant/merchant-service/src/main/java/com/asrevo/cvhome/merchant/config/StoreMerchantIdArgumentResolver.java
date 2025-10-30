package com.asrevo.cvhome.merchant.config;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.async.StandardServletAsyncWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class StoreMerchantIdArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String REQUEST_PARAMETER_STORE = "store";

	private final AccessEvaluator accessEvaluator;

	public StoreMerchantIdArgumentResolver(AccessEvaluator accessEvaluator) {
		this.accessEvaluator = accessEvaluator;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(StoreMerchantId.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		String storeCode = Optional.ofNullable(webRequest.getParameter(REQUEST_PARAMETER_STORE))
			.filter(StringUtils::isNotBlank)
			.orElseThrow(() -> new IllegalArgumentException("Missing required parameter 'store'"));

		if (isSecuredResource(webRequest)) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			boolean hasAccess = accessEvaluator.hasAccessOnStoreFindOne(authentication, new ManagerStoreId(storeCode));
			if (!hasAccess) {
				throw new UnauthorizedException("Cannot authorize user for store " + storeCode);
			}
		}

		return new StoreMerchantId(storeCode);
	}

	private boolean isSecuredResource(NativeWebRequest webRequest) {
		return ((StandardServletAsyncWebRequest) webRequest).getRequest().getRequestURI().contains("/private/");
	}

}
