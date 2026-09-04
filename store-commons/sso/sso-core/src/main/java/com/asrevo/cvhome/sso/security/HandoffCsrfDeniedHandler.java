package com.asrevo.cvhome.sso.security;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;

import lombok.RequiredArgsConstructor;

/**
 * A form posted without a valid CSRF token goes back to the login page, with a token the reader can act on and a
 * fresh cookie so the retry succeeds.
 *
 * <p>
 * The only way a real person gets here is a stale page: the cookie expired or was cleared between the hand-off and
 * the submit. Refusing with a bare 403 would strand them on a server that renders nothing they can use.
 * </p>
 */
@RequiredArgsConstructor
public class HandoffCsrfDeniedHandler implements AccessDeniedHandler {

    /** The form's token was missing or did not match: the page was stale. */
    public static final String EXPIRED = "expired";

    private final LoginPageLocator loginPages;

    private final RequestCache requestCache;

    private final CsrfTokenRepository csrfTokens;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        csrfTokens.saveToken(csrfTokens.generateToken(request), request, response);
        boolean pending = Objects.nonNull(requestCache.getRequest(request, response));
        response.sendRedirect(loginPages.loginPage(request, response, pending, EXPIRED));
    }

}
