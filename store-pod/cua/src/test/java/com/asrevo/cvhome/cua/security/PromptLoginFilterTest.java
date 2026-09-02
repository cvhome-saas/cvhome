package com.asrevo.cvhome.cua.security;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class PromptLoginFilterTest {

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String PROMPT = "prompt";

    private static final String LOGIN = "login";

    private static final String CHALLENGE = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

    private final PromptLoginFilter filter = new PromptLoginFilter();

    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private final MockFilterChain chain = new MockFilterChain();

    private static MockHttpServletRequest authorize(String prompt) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", AUTHORIZE);
        if (prompt != null) {
            request.setParameter(PROMPT, prompt);
        }
        request.setParameter("code_challenge", CHALLENGE);
        return request;
    }

    private static void signIn() {
        TestingAuthenticationToken shopper = new TestingAuthenticationToken("user", "revo", "ROLE_CUSTOMER");
        SecurityContextHolder.getContext().setAuthentication(shopper);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aSignedInSessionAskedToLogInAgainIsLoggedOutAndMarked() throws ServletException, IOException {
        signIn();
        MockHttpServletRequest request = authorize(LOGIN);
        request.getSession(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getSession(false).getAttribute(PromptLoginFilter.HANDLED)).isEqualTo(CHALLENGE);
        assertThat(chain.getRequest()).as("the chain continues, now anonymous").isNotNull();
    }

    @Test
    void theResumedRequestPassesOnceAndConsumesTheMarker() throws ServletException, IOException {
        signIn();
        MockHttpServletRequest request = authorize(LOGIN);
        request.getSession(true).setAttribute(PromptLoginFilter.HANDLED, CHALLENGE);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(request.getSession(false).getAttribute(PromptLoginFilter.HANDLED)).isNull();
    }

    /** A different flow — a new sign-in the storefront started — is prompted again, marker or not. */
    @Test
    void aDifferentFlowIsPromptedEvenThoughAnEarlierOneWasServed() throws ServletException, IOException {
        signIn();
        MockHttpServletRequest request = authorize(LOGIN);
        request.getSession(true).setAttribute(PromptLoginFilter.HANDLED, "another-challenge");

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getSession(false).getAttribute(PromptLoginFilter.HANDLED)).isEqualTo(CHALLENGE);
    }

    @Test
    void withoutPromptLoginASignedInSessionIsLeftAlone() throws ServletException, IOException {
        signIn();
        MockHttpServletRequest request = authorize(null);
        request.getSession(true);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(request.getSession(false).getAttribute(PromptLoginFilter.HANDLED)).isNull();
    }

    /** The prompt counts as served for an anonymous arrival too; otherwise the resumed request would be logged out. */
    @Test
    void anAnonymousRequestIsMarkedSoTheResumedOnePasses() throws ServletException, IOException {
        MockHttpServletRequest request = authorize("consent login");

        filter.doFilter(request, response, chain);

        assertThat(request.getSession(false).getAttribute(PromptLoginFilter.HANDLED)).isEqualTo(CHALLENGE);
        assertThat(chain.getRequest()).isNotNull();
    }

}
