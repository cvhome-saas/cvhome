package com.asrevo.cvhome.sso.audit;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * The ip and user agent of the request being served, if there is one.
 *
 * <p>
 * {@code getRemoteAddr()} is the client's address rather than the gateway's because
 * {@code server.forward-headers-strategy: NATIVE} lets the container honour {@code X-Forwarded-For}.
 * </p>
 */
public record AuditRequestContext(String ip, String userAgent) {

    public static final AuditRequestContext NONE = new AuditRequestContext(null, null);

    static final int MAX_USER_AGENT = 512;

    public static AuditRequestContext current() {
        return request().map(r -> new AuditRequestContext(r.getRemoteAddr(), truncate(r.getHeader("User-Agent"))))
                .orElse(NONE);
    }

    public static Optional<HttpServletRequest> request() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return Optional.of(attributes.getRequest());
        }
        return Optional.empty();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_USER_AGENT) {
            return value;
        }
        return value.substring(0, MAX_USER_AGENT);
    }

}
