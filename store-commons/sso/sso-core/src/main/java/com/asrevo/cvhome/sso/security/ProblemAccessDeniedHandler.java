package com.asrevo.cvhome.sso.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * What a request the filter chain refuses gets back.
 *
 * <p>
 * Written directly, never through {@code sendError}: the container's error dispatch lands on {@code /error}, and
 * anything that answers that path with a 200 — a single-page app's index route did — turns a refusal into a page. So
 * the 403 is a {@code application/problem+json} body from the shared factory, the same shape every controller
 * refusal already has, which also closes the gap where a chain-level 403 carried no body at all.
 * </p>
 *
 * <p>
 * The one exception is the sign-in form itself: a person who submits a tab left open past the session posts a stale
 * CSRF token, and a JSON body would be the wrong answer to a form. That request goes back to {@code /login} with
 * {@code ?error=expired}, where a fresh cookie is planted and the page can say what happened.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    static final String LOGIN = "/login";

    static final String EXPIRED = "/login?error=expired";

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        if (exception instanceof CsrfException && LOGIN.equals(request.getServletPath())) {
            response.setStatus(HttpStatus.FOUND.value());
            response.setHeader("Location", String.format("%s%s", request.getContextPath(), EXPIRED));
            return;
        }
        ProblemDetail problem = problems.create(CommonErrors.ACCESS_DENIED, "Access is denied.", Map.of(), List.of(),
                problems.traceId());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), problem);
    }

}
