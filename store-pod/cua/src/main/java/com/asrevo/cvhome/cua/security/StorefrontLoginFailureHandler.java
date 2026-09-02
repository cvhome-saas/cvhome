package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * A failed sign-in goes back to the storefront's login page, marked as still pending and carrying an error token.
 *
 * <p>
 * The token, not a message: cua has no strings, and the storefront renders the failure in the shopper's own
 * language. The saved authorize request is left in the session so the shopper can simply try again.
 * </p>
 */
public class StorefrontLoginFailureHandler implements AuthenticationFailureHandler {

    /** The username or password was wrong. */
    public static final String INVALID = "invalid";

    /** A social provider refused, or the callback could not be completed. */
    public static final String SOCIAL = "social";

    private final RequestCache requestCache;

    private final String error;

    public StorefrontLoginFailureHandler(RequestCache requestCache, String error) {
        this.requestCache = requestCache;
        this.error = error;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.sendRedirect(StorefrontUrls.loginPage(request, response, requestCache, true, error));
    }

}
