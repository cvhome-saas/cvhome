package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;
import java.lang.annotation.Annotation;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ReactiveOrgStorePrincipalInfoArgumentResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return findMethodAnnotation(OrgStorePrincipalInfo.class, parameter) != null;
    }

    @Override
    public Mono<Object> resolveArgument(
            MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(
                        (authentication) ->
                                Mono.justOrEmpty(resolvePrincipal(parameter, authentication)));
    }

    private Object resolvePrincipal(MethodParameter parameter, Authentication authentication) {
        return SecurityUtils.getOrgStoreIdentity(authentication);
    }

    /**
     * Obtains the specified {@link Annotation} on the specified {@link MethodParameter}.
     *
     * @param annotationClass the class of the {@link Annotation} to find on the
     *                        {@link MethodParameter}
     * @param parameter       the {@link MethodParameter} to search for an {@link Annotation}
     * @return the {@link Annotation} that was found or null.
     */
    private <T extends Annotation> T findMethodAnnotation(
            Class<T> annotationClass, MethodParameter parameter) {
        T annotation = parameter.getParameterAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        Annotation[] annotationsToSearch = parameter.getParameterAnnotations();
        for (Annotation toSearch : annotationsToSearch) {
            annotation = AnnotationUtils.findAnnotation(toSearch.annotationType(), annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }
}
