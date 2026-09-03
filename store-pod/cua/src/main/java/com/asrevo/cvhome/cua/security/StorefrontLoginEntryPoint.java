package com.asrevo.cvhome.cua.security;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.RequestCache;

import lombok.RequiredArgsConstructor;

/**
 * Where an unauthenticated shopper is sent: the storefront's login page, with the marker that says cua is holding
 * their authorize request.
 *
 * <p>
 * Replaces {@code LoginUrlAuthenticationEntryPoint("/login")}, which resolved against cua's own context path and
 * landed on a Thymeleaf page nothing could theme. The saved request has already been stored by the time this runs,
 * so once the storefront posts the credentials back to {@code /cua/login} the flow resumes where it left off.
 * </p>
 *
 * <p>
 * The redirect also plants the CSRF cookie. cua never renders the form, so this is the one response that reaches
 * the browser before it does; the storefront reads the cookie server-side and puts the token into the form as a
 * hidden input, which keeps the hand-off free of JavaScript.
 * </p>
 */
@RequiredArgsConstructor
public class StorefrontLoginEntryPoint implements AuthenticationEntryPoint {

    private final RequestCache requestCache;

    private final CsrfTokenRepository csrfTokens;

    /** Issues the CSRF cookie when the browser does not already hold one. */
    public static void plantCsrfCookie(CsrfTokenRepository csrfTokens, HttpServletRequest request, HttpServletResponse response) {
        if (Objects.isNull(csrfTokens.loadToken(request))) {
            csrfTokens.saveToken(csrfTokens.generateToken(request), request, response);
        }
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        plantCsrfCookie(csrfTokens, request, response);
        response.sendRedirect(StorefrontUrls.loginPage(request, response, requestCache, true, null));
    }

}
