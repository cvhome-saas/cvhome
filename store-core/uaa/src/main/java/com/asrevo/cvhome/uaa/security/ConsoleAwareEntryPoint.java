package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

/**
 * Which sign-in page an unauthenticated browser is sent to, decided per request.
 *
 * <p>
 * Behind the console it is the console's, reached through the shared hand-off — which also plants the CSRF cookie,
 * because that redirect is the last thing uaa sends before somebody else renders the form. On uaa's own host it is
 * uaa's own page, unchanged: this is the door a platform administrator uses and it must keep working exactly as it
 * did.
 * </p>
 */
@RequiredArgsConstructor
public class ConsoleAwareEntryPoint implements AuthenticationEntryPoint {

    private final ConsoleUrls console;

    private final AuthenticationEntryPoint handoff;

    private final AuthenticationEntryPoint own;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        if (console.isHandoff(request)) {
            handoff.commence(request, response, exception);
            return;
        }
        own.commence(request, response, exception);
    }

}
