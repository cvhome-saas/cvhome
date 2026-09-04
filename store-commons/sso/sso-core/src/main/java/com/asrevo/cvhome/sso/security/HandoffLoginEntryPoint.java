package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import lombok.RequiredArgsConstructor;

/**
 * Where an unauthenticated browser is sent: the page this deployment hands off to, marked as pending.
 *
 * <p>
 * Replaces {@code LoginUrlAuthenticationEntryPoint}, which resolves its target against the server's own context
 * path — right for a server that renders its own page, wrong for one whose page is served by another application
 * on the same origin. The saved request has already been stored by the time this runs, so once the credentials are
 * posted back the flow resumes where it left off.
 * </p>
 *
 * <p>
 * The redirect also plants the CSRF cookie. This is the one response that reaches the browser before the form is
 * rendered, and the page that renders it can only echo a token it can read, so the cookie has to exist by now.
 * </p>
 */
@RequiredArgsConstructor
public class HandoffLoginEntryPoint implements AuthenticationEntryPoint {

    private final LoginPageLocator loginPages;

    private final CsrfTokenRepository csrfTokens;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        HandoffUrls.plantCsrfCookie(csrfTokens, request, response);
        response.sendRedirect(loginPages.loginPage(request, response, true, null));
    }

}
