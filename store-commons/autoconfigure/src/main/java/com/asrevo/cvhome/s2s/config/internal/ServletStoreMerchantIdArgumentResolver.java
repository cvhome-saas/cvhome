package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

			if (authentication == null || !authentication.isAuthenticated()) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
			}

			if (!hasRequiredRoles(authentication, parameter)) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
			}

			boolean hasAccess = accessEvaluator.hasAccessOnStoreFindOne(authentication, new ManagerStoreId(storeCode));
			if (!hasAccess) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
			}
		}

		return new StoreMerchantId(storeCode);
	}

	private boolean isSecuredResource(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(SecuredResource.class);
	}

	public boolean hasRequiredRoles(Authentication authentication, MethodParameter parameter) {
		Roles[] requiredRoles = getSecuredRoles(parameter);
		if (requiredRoles.length == 0) {
			return false;
		}
		return containAnyRequiredRoles(authentication.getAuthorities(), requiredRoles);
	}

	private Roles[] getSecuredRoles(MethodParameter parameter) {
		SecuredResource annotation = parameter.getParameterAnnotation(SecuredResource.class);
		return annotation != null ? annotation.roles() : new Roles[0];
	}

	private boolean containAnyRequiredRoles(Collection<? extends GrantedAuthority> authorities, Roles[] requiredRoles) {
		if (authorities == null || authorities.isEmpty() || requiredRoles == null || requiredRoles.length == 0) {
			return false;
		}

		Set<String> authoritiesSet = authorities.stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.toSet());

		return Arrays.stream(requiredRoles).map(Enum::name).anyMatch(authoritiesSet::contains);
	}

}
