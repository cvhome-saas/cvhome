package com.asrevo.cvhome.sso.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Makes the server agree with the proxy about what its own address is.
 *
 * <p>
 * Both deployments are reached under a path prefix on somebody else's origin — cua at {@code /cua*} on the
 * shopper's store domain, uaa at {@code /uaa/**} on the console's — and both are reached directly as well. The
 * proxy says which by sending {@code X-Forwarded-Prefix}; without it, every absolute URL the server builds drops
 * the prefix, so a redirect to {@code /login} lands on the fronting application's own {@code /login} instead of
 * the server's, and the session cookie is written at {@code /} where it collides with the front end's.
 * </p>
 *
 * <p>
 * Reporting the prefix as the context path fixes both at once: Spring maps {@code /cua/login} to {@code /login},
 * and the container writes the session cookie under {@code /cua/}. It runs at {@code HIGHEST_PRECEDENCE} so
 * everything downstream — the session repository filter included — sees the corrected request.
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PathPrefixFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String prefix = request.getHeader("X-Forwarded-Prefix");

        if (StringUtils.hasText(prefix)) {
            filterChain.doFilter(new PrefixRequestWrapper(request, prefix), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    static class PrefixRequestWrapper extends HttpServletRequestWrapper {

        private final String prefix;

        PrefixRequestWrapper(HttpServletRequest request, String prefix) {
            super(request);
            this.prefix = prefix;
        }

        @Override
        public String getContextPath() {
            return prefix;
        }

        /**
         * The address the browser actually used.
         *
         * <p>
         * The prefix is only prepended when the request path does not already carry it. Both proxies forward the
         * path intact — spg with {@code handle /cua*} rather than {@code handle_path}, store-core-gateway by not
         * stripping {@code /uaa} — so the URI normally has it, and appending it again produced
         * {@code /uaa/uaa/login}. That combination is not optional either: Spring rejects a context path that is
         * not the literal start of the request path, so "stripped, and named in the header" cannot occur.
         * </p>
         */
        @Override
        public StringBuffer getRequestURL() {
            String uri = getRequestURI();
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            if (getServerPort() != 80 && getServerPort() != 443) {
                url.append(":").append(getServerPort());
            }
            if (!uri.startsWith(prefix)) {
                url.append(prefix);
            }
            url.append(uri);
            return url;
        }

    }

}