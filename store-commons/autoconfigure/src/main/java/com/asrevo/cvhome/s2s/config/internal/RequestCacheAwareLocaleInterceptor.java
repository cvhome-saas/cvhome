package com.asrevo.cvhome.s2s.config.internal;

import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestCacheAwareLocaleInterceptor extends LocaleChangeInterceptor {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException {

        // 1. Try to get locale from the current request first (?lang=en)
        String newLocale = request.getParameter(getParamName());

        // 2. If not found, check the RequestCache (Saved Request after Login)
        if (newLocale == null) {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            newLocale = Optional.ofNullable(savedRequest)
                    .flatMap(it -> Optional.ofNullable(it.getParameterValues(getParamName())))
                    .filter(it -> it.length > 0)
                    .map(it -> it[0])
                    .orElse(null);
        }

        // 3. If we found a locale in either place, let the parent logic handle the
        // resolution
        if (newLocale != null) {
            // We temporarily "inject" the parameter logic by calling super or
            // manually triggering the resolver as the parent does.
            // Since parent preHandle logic relies on request.getParameter(),
            // we manually call the resolver here if we found it in the cache.

            updateLocale(request, response, newLocale);
        }

        return true;
    }

    /**
     * Applies the locale, tolerating a resolver that does not accept one.
     *
     * <p>
     * <strong>Not every service can honour this, and that must not be an error.</strong> The pod services each
     * declare a {@code SessionLocaleResolver}, so setting a locale works there. The store-core services declare
     * none and therefore get Spring Boot's default {@code AcceptHeaderLocaleResolver}, whose {@code setLocale}
     * throws {@link UnsupportedOperationException} by contract — the locale comes from the request's own
     * Accept-Language header and cannot be overridden.
     * </p>
     *
     * <p>
     * Because this interceptor is registered for every service from {@code autoconfigure}, letting that escape
     * turned a whole service's web layer into 500s the moment anything supplied a locale — a {@code ?lang=}
     * parameter, or a request cached by Spring Security across a login. It read as intermittent because it only
     * fires when a locale is actually found.
     * </p>
     *
     * <p>
     * Swallowing it is right rather than merely convenient: language for these APIs is carried explicitly by
     * {@code ServletLanguageCodeArgumentResolver} from the {@code lang} parameter, so the framework locale is a
     * convenience for message resolution, not the mechanism the endpoints read.
     * </p>
     */
    private void updateLocale(HttpServletRequest request, HttpServletResponse response, String localeValue) {
        try {
            Locale locale = parseLocaleValue(localeValue);
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                localeResolver.setLocale(request, response, locale);
            }
        } catch (IllegalArgumentException ex) {
            if (!isIgnoreInvalidLocale()) {
                throw ex;
            }
        } catch (UnsupportedOperationException ex) {
            log.debug("This service's LocaleResolver does not accept a locale; keeping the request's own", ex);
        }
    }

}