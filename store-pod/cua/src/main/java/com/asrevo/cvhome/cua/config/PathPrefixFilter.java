package com.asrevo.cvhome.cua.config;

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

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = new StringBuffer();
            url.append(getScheme()).append("://").append(getServerName());
            if (getServerPort() != 80 && getServerPort() != 443) {
                url.append(":").append(getServerPort());
            }
            url.append(prefix).append(getRequestURI());
            return url;
        }

    }

}