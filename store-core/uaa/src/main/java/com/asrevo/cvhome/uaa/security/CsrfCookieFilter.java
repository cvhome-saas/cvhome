package com.asrevo.cvhome.uaa.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Makes sure the {@code XSRF-TOKEN} cookie is written on every response.
 *
 * <p>
 * {@code CookieCsrfTokenRepository} only writes the cookie when something reads the token, and a single-page app's
 * first request is for {@code index.html}, which reads nothing. Touching the token here means the cookie is present
 * before the sign-in form posts and before the console's first {@code fetch}; Angular's {@code HttpClient} then copies
 * it into {@code X-XSRF-TOKEN} on its own.
 * </p>
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();
        }
        chain.doFilter(request, response);
    }

}
