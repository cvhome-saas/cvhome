package com.asrevo.cvhome.sso.security;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginFailureHandlerTest {

    private static final String USERNAME = "someone";

    private static final String LOCKED = "/login?error=locked";

    private static final BadCredentialsException BAD = new BadCredentialsException("bad");

    private final LockoutService lockout = mock(LockoutService.class);

    private final LoginFailureHandler handler = new LoginFailureHandler(lockout);

    @Test
    void badCredentialsCarryTheAttemptsLeft() throws IOException {
        when(lockout.attemptsLeft(USERNAME)).thenReturn(3);

        assertThat(redirectFor(BAD)).isEqualTo("/login?error&attemptsLeft=3");
    }

    @Test
    void theLockingAttemptIsReportedAsLocked() throws IOException {
        when(lockout.attemptsLeft(USERNAME)).thenReturn(0);

        assertThat(redirectFor(BAD)).isEqualTo(LOCKED);
    }

    @Test
    void unknownUsersGetNoCounter() throws IOException {
        when(lockout.attemptsLeft(USERNAME)).thenReturn(-1);

        assertThat(redirectFor(BAD)).isEqualTo("/login?error");
    }

    @Test
    void accountStateExceptionsMapToTheirOwnCodes() throws IOException {
        assertThat(redirectFor(new LockedException("locked"))).isEqualTo(LOCKED);
        assertThat(redirectFor(new DisabledException("disabled"))).isEqualTo("/login?error=disabled");
    }

    private String redirectFor(AuthenticationException exception) throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setParameter(LoginFailureHandler.USERNAME, USERNAME);
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationFailure(request, response, exception);
        return response.getRedirectedUrl();
    }

}
