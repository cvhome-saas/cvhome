package com.asrevo.cvhome.uaa.security;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.sso.security.HandoffCsrfDeniedHandler;
import com.asrevo.cvhome.sso.security.HandoffLoginEntryPoint;

import lombok.RequiredArgsConstructor;

/**
 * uaa's console-facing edge, assembled in one place.
 *
 * <p>
 * Each piece is a pair — the console's answer and uaa's own — chosen per request by {@link ConsoleUrls#isHandoff}.
 * They are gathered here rather than built inline in the filter chain because there are three of them and the
 * chain already takes as many arguments as the house style allows; keeping them together also makes it one file
 * to read when the question is "what does uaa do differently when the console is in front".
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ConsoleEdge {

    private static final String OWN_LOGIN_PAGE = "/login";

    private final ConsoleUrls console;

    private final RequestCache requestCache;

    private final CookieCsrfTokenRepository csrfCookies;

    /** Every sign-in redirect, sent to the console when the console is in front. */
    public RedirectStrategy redirects() {
        return new ConsoleRedirectStrategy(console);
    }

    /** Where an unauthenticated browser is sent: the console's page, or uaa's own. */
    public AuthenticationEntryPoint entryPoint() {
        return new ConsoleAwareEntryPoint(console, new HandoffLoginEntryPoint(console, csrfCookies),
                new LoginUrlAuthenticationEntryPoint(OWN_LOGIN_PAGE));
    }

    /** A stale form goes back to the console; every other refusal keeps its problem+json body. */
    public AccessDeniedHandler accessDenied(AccessDeniedHandler problems) {
        return new ConsoleAwareAccessDeniedHandler(console,
                new HandoffCsrfDeniedHandler(console, requestCache, csrfCookies), problems);
    }

}
