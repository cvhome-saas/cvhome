package com.asrevo.cvhome.cua.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Makes {@code prompt=login} mean what OpenID Connect says it means: the shopper types their password again.
 *
 * <p>
 * The storefront sends {@code prompt=login} on every sign-in, and the authorization server answered it from a
 * live session anyway — so a shopper still signed in as A who registered account B was handed straight to the
 * callback as A. This filter sits in the authorization-server chain, after the security context has been
 * loaded: an authenticated {@code /oauth2/authorize} carrying {@code prompt=login} is logged out first (session
 * invalidated, context cleared), which makes the rest of the chain save the request and send the shopper to
 * the storefront's form. The session then remembers which authorize request the prompt was served for — its
 * {@code code_challenge}, which the storefront regenerates on every sign-in — so the request that resumes after
 * the form is posted passes through, while a new flow started later is prompted again.
 * </p>
 */
public class PromptLoginFilter extends OncePerRequestFilter {

    static final String HANDLED = String.format("%s.HANDLED", PromptLoginFilter.class.getName());

    private static final String AUTHORIZE = "/oauth2/authorize";

    private static final String PROMPT = "prompt";

    private static final String LOGIN = "login";

    private static final String CODE_CHALLENGE = "code_challenge";

    private static final String STATE = "state";

    private final SecurityContextLogoutHandler logout = new SecurityContextLogoutHandler();

    private static boolean promptsLogin(HttpServletRequest request) {
        String prompt = request.getParameter(PROMPT);
        return Objects.nonNull(prompt) && Arrays.asList(prompt.split(" ")).contains(LOGIN);
    }

    /** What identifies this authorize request across the hand-off: the PKCE challenge, else the state, else nothing. */
    static String flowKey(HttpServletRequest request) {
        String challenge = request.getParameter(CODE_CHALLENGE);
        if (Objects.nonNull(challenge)) {
            return challenge;
        }
        return Objects.requireNonNullElse(request.getParameter(STATE), "");
    }

    private static boolean signedIn(Authentication authentication) {
        return Objects.nonNull(authentication) && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.GET.matches(request.getMethod()) || !request.getRequestURI().endsWith(AUTHORIZE)
                || !promptsLogin(request);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        String flow = flowKey(request);
        if (Objects.nonNull(session) && flow.equals(session.getAttribute(HANDLED))) {
            // The form has just been posted and this very request is resuming: the prompt was served.
            session.removeAttribute(HANDLED);
            chain.doFilter(request, response);
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (signedIn(authentication)) {
            logout.logout(request, response, authentication);
        }
        // Served for this flow: the chain below saves the request and sends the shopper to the form.
        request.getSession(true).setAttribute(HANDLED, flow);
        chain.doFilter(request, response);
    }

}
