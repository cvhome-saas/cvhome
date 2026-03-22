package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
public class ServletStoreMerchantIdArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String REQUEST_PARAMETER_STORE = "store";

	private final AccessEvaluator accessEvaluator;

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

		if (isSecuredResource(parameter)) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			boolean hasAccess = accessEvaluator.hasAccessOnStoreFindOne(authentication, new ManagerStoreId(storeCode));
			if (!hasAccess) {
				// throw new AccessDeniedException("Cannot authorize user for store " +
				// storeCode);
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
			}
		}

		return new StoreMerchantId(storeCode);
	}

	private boolean isSecuredResource(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(SecuredResource.class);
	}

}
