package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Where a failed sign-in goes: back to the page, with a code the page can explain.
 *
 * <p>
 * {@code locked}, {@code disabled} and {@code expired} are states the person needs to know about — waiting will
 * not help a disabled account, and an administrator is who to ask. A wrong password stays {@code invalid} with the
 * attempts left before the next lock, and nothing about which half was wrong.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    static final String LOGIN = "/login";

    static final String USERNAME = "username";

    static final String LOCKED = String.format("%s?error=locked", LOGIN);

    static final String FAILED = String.format("%s?error", LOGIN);

    private final LockoutService lockout;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String target;
        if (exception instanceof LockedException) {
            target = LOCKED;
        } else if (exception instanceof DisabledException) {
            target = String.format("%s?error=disabled", LOGIN);
        } else if (exception instanceof CredentialsExpiredException) {
            target = String.format("%s?error=expired-password", LOGIN);
        } else {
            target = badCredentialsTarget(request.getParameter(USERNAME));
        }
        saveException(request, exception);
        getRedirectStrategy().sendRedirect(request, response, target);
    }

    /**
     * The attempt that crosses the threshold is reported as the lock it caused, not as "0 attempts left": the
     * listener has already locked the account by the time this handler runs, so the next attempt would be
     * refused whatever the password.
     */
    private String badCredentialsTarget(String username) {
        int left = username == null ? -1 : lockout.attemptsLeft(username);
        if (left < 0) {
            return FAILED;
        }
        return left == 0 ? LOCKED : String.format("%s?error&attemptsLeft=%d", LOGIN, left);
    }

}
