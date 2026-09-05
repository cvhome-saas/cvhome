package com.asrevo.cvhome.sso.security;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.csrf.CsrfException;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What a request the filter chain refuses gets back.
 *
 * <p>
 * The body is written straight to the response rather than through {@code sendError}, because the container's error
 * dispatch lands on {@code /error} — and anything answering that path with a 200, as a single-page app's index route
 * did, turns a refusal into a page. So the assertion here is on the status <em>and</em> the content type: a 403 that
 * is not {@code application/problem+json} is the regression.
 * </p>
 *
 * <p>
 * The sign-in form is the one exception. A tab left open past the session posts a stale CSRF token, and JSON is the
 * wrong answer to a form post, so that one redirects to {@code /login?error=expired} where a fresh cookie is
 * planted. It is scoped to {@code /login}: a CSRF failure anywhere else still gets the problem body.
 * </p>
 */
class ProblemAccessDeniedHandlerTest {

    private static final String LOGIN = "/login";
    private static final String CONTEXT = "/sso";

    private static final String ADMIN_USERS = "/admin/users";

    private static final String CONCAT = "%s%s";
    private static final String TRACE_ID = "trace-1";
    private static final String DENIED = "denied";
    private static final String ACCESS_DENIED = "Access is denied.";
    private static final String STALE = "stale token";
    private static final String LOCATION = "Location";

    private final ProblemDetailFactory problems = mock(ProblemDetailFactory.class);
    private final ProblemAccessDeniedHandler handler =
            new ProblemAccessDeniedHandler(problems, JsonMapper.builder().build());

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void arefusalIsAproblemJsonBodyFromTheSharedFactoryRatherThanAnEmptyForbidden() throws Exception {
        givenAproblem();

        handler.handle(request, response, new AccessDeniedException(DENIED));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertThat(response.getContentAsString()).contains(ACCESS_DENIED);
    }

    @Test
    void astaleCsrfTokenOnTheSignInFormGoesBackToTheFormRatherThanGettingJson() throws Exception {
        request.setServletPath(LOGIN);

        handler.handle(request, response, new CsrfException(STALE));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(response.getHeader(LOCATION)).isEqualTo(ProblemAccessDeniedHandler.EXPIRED);
    }

    @Test
    void thatRedirectKeepsTheContextPathSoItWorksWhenTheAppIsNotAtTheRoot() throws Exception {
        request.setServletPath(LOGIN);
        request.setContextPath(CONTEXT);

        handler.handle(request, response, new CsrfException(STALE));

        assertThat(response.getHeader(LOCATION))
                .isEqualTo(CONCAT.formatted(CONTEXT, ProblemAccessDeniedHandler.EXPIRED));
    }

    @Test
    void acsrfFailureAnywhereButTheSignInFormStillGetsTheProblemBody() throws Exception {
        givenAproblem();
        request.setServletPath(ADMIN_USERS);

        handler.handle(request, response, new CsrfException(STALE));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void arefusalThatIsNotAcsrfFailureOnTheSignInPathIsStillAproblemBody() throws Exception {
        givenAproblem();
        request.setServletPath(LOGIN);

        handler.handle(request, response, new AccessDeniedException(DENIED));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    private void givenAproblem() {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ACCESS_DENIED);
        when(problems.traceId()).thenReturn(TRACE_ID);
        when(problems.create(eq(CommonErrors.ACCESS_DENIED), anyString(), anyMap(), anyList(), any()))
                .thenReturn(problem);
    }

    /**
     * cua is reached at {@code /cua*} on the shopper's own store host, so the sign-in form's CSRF failure arrives
     * with a context path.
     *
     * <p>
     * This compared the raw {@code getServletPath()} — {@code "/cua/login"} — against {@code "/login"}, which is
     * false, so the redirect back to the form never happened and a shopper with a tab left open past the session
     * got a JSON problem body their browser rendered as nothing. The prefix has to come off first.
     * </p>
     */
    @Test
    void astaleTokenOnTheFormBehindAproxyPrefixIsStillSentBackToTheForm() throws Exception {
        request.setContextPath(CONTEXT);
        request.setServletPath(CONCAT.formatted(CONTEXT, LOGIN));

        handler.handle(request, response, new CsrfException(STALE));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(response.getHeader(LOCATION))
                .isEqualTo(CONCAT.formatted(CONTEXT, ProblemAccessDeniedHandler.EXPIRED));
    }

    @Test
    void arefusalOnAnotherPathBehindTheSamePrefixIsStillAproblemBody() throws Exception {
        givenAproblem();
        request.setContextPath(CONTEXT);
        request.setServletPath(CONCAT.formatted(CONTEXT, ADMIN_USERS));

        handler.handle(request, response, new CsrfException(STALE));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

}
