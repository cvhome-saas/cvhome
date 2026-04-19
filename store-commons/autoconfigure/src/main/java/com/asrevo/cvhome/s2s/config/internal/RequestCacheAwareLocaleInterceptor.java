package com.asrevo.cvhome.s2s.config.internal;

import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

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

    private void updateLocale(HttpServletRequest request, HttpServletResponse response, String localeValue) {
        try {
            Locale locale = parseLocaleValue(localeValue);
            var localeResolver = org.springframework.web.servlet.support.RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                localeResolver.setLocale(request, response, locale);
            }
        } catch (IllegalArgumentException ex) {
            if (!isIgnoreInvalidLocale()) {
                throw ex;
            }
        }
    }

}