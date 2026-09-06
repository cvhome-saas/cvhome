package com.asrevo.cvhome.metrics;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Counts every 401 and 403 the service answers, with <em>why</em>, as {@code cvhome.auth.rejections}.
 *
 * <p>
 * The HTTP request meters already carry the status, but not the reason, and a 401 decided by the JWT filter never
 * reaches a controller advice: it has no body, no route and no log line. This filter sits in front of the security
 * chain and reads the outcome on the way back — the {@code WWW-Authenticate} header names the bearer-token failure
 * ({@code invalid_token}, {@code insufficient_scope}, or nothing at all when no token was sent), and
 * {@code SecurityErrorHandler} leaves the exception name in a request attribute for the rejections it rendered
 * itself. Tags: {@code status}, {@code reason}, {@code source} (filter or advice) and the matched route
 * ({@code UNKNOWN} when the request was refused before routing).
 * </p>
 */
public class AuthRejectionMetricsFilter extends OncePerRequestFilter {

    /**
     * Request attribute the advice sets to name the rejection it rendered.
     */
    public static final String REASON_ATTRIBUTE = "cvhome.auth.reason";

    static final String METER = "cvhome.auth.rejections";

    static final String REASON_MISSING_TOKEN = "missing_token";

    static final String REASON_UNKNOWN = "unknown";

    static final String SOURCE_FILTER = "filter";

    static final String SOURCE_ADVICE = "advice";

    private static final String UNKNOWN_ROUTE = "UNKNOWN";

    private static final Pattern BEARER_ERROR = Pattern.compile("error=\"([^\"]+)\"");

    private final MeterRegistry registry;

    public AuthRejectionMetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            record(request, response);
        }
    }

    private void record(HttpServletRequest request, HttpServletResponse response) {
        int status = response.getStatus();
        if (status != HttpStatus.UNAUTHORIZED.value() && status != HttpStatus.FORBIDDEN.value()) {
            return;
        }
        Object fromAdvice = request.getAttribute(REASON_ATTRIBUTE);
        String source = fromAdvice == null ? SOURCE_FILTER : SOURCE_ADVICE;
        String reason = fromAdvice == null ? bearerError(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
                : fromAdvice.toString();
        String route = Optional.ofNullable(request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE))
                .map(Object::toString).orElse(UNKNOWN_ROUTE);
        Counter.builder(METER)
                .description("Requests answered 401 or 403, by reason and where the decision was made")
                .tags("status", Integer.toString(status), "reason", reason, "source", source, "uri", route)
                .register(registry)
                .increment();
    }

    /**
     * The bearer-token failure named by a {@code WWW-Authenticate} challenge, {@code missing_token} for a challenge
     * that names none (no token was presented), {@code unknown} when there is no challenge at all.
     */
    static String bearerError(String challenge) {
        if (challenge == null) {
            return REASON_UNKNOWN;
        }
        Matcher matcher = BEARER_ERROR.matcher(challenge);
        return matcher.find() ? matcher.group(1) : REASON_MISSING_TOKEN;
    }

}
