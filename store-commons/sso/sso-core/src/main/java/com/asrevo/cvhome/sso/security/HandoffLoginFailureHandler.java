package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import lombok.RequiredArgsConstructor;

/**
 * A failed sign-in goes back to the login page, marked as still pending and carrying an error token.
 *
 * <p>
 * The token, not a message: the page belongs to another application, which renders the failure in the reader's own
 * language. The saved authorize request is left in the session, so trying again simply works.
 * </p>
 */
@RequiredArgsConstructor
public class HandoffLoginFailureHandler implements AuthenticationFailureHandler {

    /** The username or password was wrong. */
    public static final String INVALID = "invalid";

    /** A brokered provider refused, or the callback could not be completed. */
    public static final String SOCIAL = "social";

    private final LoginPageLocator loginPages;

    private final String error;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.sendRedirect(loginPages.loginPage(request, response, true, error));
    }

}
