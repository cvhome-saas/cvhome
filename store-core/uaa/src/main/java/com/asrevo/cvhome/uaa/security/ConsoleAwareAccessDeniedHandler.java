package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;

import com.asrevo.cvhome.sso.security.HandoffUrls;

import lombok.RequiredArgsConstructor;

/**
 * A stale sign-in form behind the console goes back to the console; every other refusal is answered as before.
 *
 * <p>
 * The narrow condition is deliberate. Only a CSRF failure on the sign-in POST is a person with a page left open
 * too long — a refusal anywhere else is a real refusal, and answering it with a redirect would turn a 403 an API
 * caller has to see into a login page it cannot read. That is the same guard {@code ProblemAccessDeniedHandler}
 * already makes for uaa's own page; this adds the console's origin to it.
 * </p>
 */
@RequiredArgsConstructor
public class ConsoleAwareAccessDeniedHandler implements AccessDeniedHandler {

    private static final String LOGIN = "/login";

    private final ConsoleUrls console;

    private final AccessDeniedHandler handoff;

    private final AccessDeniedHandler problems;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException, ServletException {
        if (console.isHandoff(request) && exception instanceof CsrfException
                && LOGIN.equals(HandoffUrls.pathWithinApplication(request))) {
            handoff.handle(request, response, exception);
            return;
        }
        problems.handle(request, response, exception);
    }

}
