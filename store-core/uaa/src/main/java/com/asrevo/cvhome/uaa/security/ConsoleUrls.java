package com.asrevo.cvhome.uaa.security;

import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.sso.security.HandoffUrls;
import com.asrevo.cvhome.sso.security.LoginPageLocator;
import com.asrevo.cvhome.uaa.config.ConsoleProperties;

import lombok.RequiredArgsConstructor;

/**
 * uaa's two front doors, and the one rule that tells them apart.
 *
 * <p>
 * A request that arrived through store-core-gateway carries the console's path prefix as its context path — that
 * is {@code PathPrefixFilter} turning {@code X-Forwarded-Prefix} into something Spring understands — and it is
 * the only signal that can be trusted here. It cannot be spoofed by a query parameter, it is the same value the
 * container used to scope the session cookie, and it says exactly what the answer depends on: whether the browser
 * is on the console's origin, where the console's pages exist, or on uaa's own, where they do not.
 * </p>
 *
 * <p>
 * The origin is read off the request rather than configured, so a person who started on {@code gateway.com} is
 * handed back to {@code gateway.com} and one who started on {@code console-ui.gateway.com} to that — the console
 * answers on three hosts and the sign-in has to come home to the right one.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ConsoleUrls implements LoginPageLocator {

    /** Tells the console that uaa is holding a saved authorize request, so it renders the form. */
    public static final String PENDING_PARAM = "auth";

    public static final String PENDING_VALUE = "1";

    public static final String ERROR_PARAM = "error";

    private final ConsoleProperties console;

    /**
     * Whether this request came in behind the console.
     *
     * <p>
     * Reached directly on uaa's host the context path is empty and this is false, which is what keeps
     * {@code uaa.gateway.com:8001/login} serving uaa's own page to the platform administrator.
     * </p>
     */
    public boolean isHandoff(HttpServletRequest request) {
        return console.isEnabled() && console.getPathPrefix().equals(request.getContextPath());
    }

    @Override
    public String loginPage(HttpServletRequest request, HttpServletResponse response, boolean pending, String error) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(HandoffUrls.origin(request))
                .path(console.getSignInPage());
        if (pending) {
            builder.queryParam(PENDING_PARAM, PENDING_VALUE);
        }
        if (Objects.nonNull(error)) {
            builder.queryParam(ERROR_PARAM, error);
        }
        return builder.build().toUriString();
    }

    /** The console's sign-in page for this request, carrying a query uaa already built, verbatim. */
    public String signInWithQuery(HttpServletRequest request, String query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(HandoffUrls.origin(request))
                .path(console.getSignInPage())
                .queryParam(PENDING_PARAM, PENDING_VALUE);
        if (Objects.nonNull(query) && !query.isEmpty()) {
            builder.query(query);
        }
        return builder.build().toUriString();
    }

    /** The console's sign-in page with nothing pending: a fresh flow starts there. */
    public String signIn(HttpServletRequest request) {
        return UriComponentsBuilder.fromUriString(HandoffUrls.origin(request))
                .path(console.getSignInPage())
                .build()
                .toUriString();
    }

}
