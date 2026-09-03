package com.asrevo.cvhome.uaa.ratelimit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.uaa.audit.AuditActor;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.errors.UaaErrors;

import tools.jackson.databind.ObjectMapper;

/**
 * Refuses a burst of attempts from one address on the endpoints that take a secret.
 *
 * <p>
 * Runs before Spring Security, so a refused attempt costs no password hash and no event. The 429 carries the
 * shared problem shape from the factory — the one place in uaa a body is written outside the advice — and a
 * {@code Retry-After} of the window. Only POSTs count: loading the sign-in page is not an attempt.
 * </p>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    static final String LOGIN = "/login";

    static final String TOKEN = "/oauth2/token";

    static final String PUBLIC_PREFIX = "/api/v1/public/";

    static final String RETRY_AFTER = "Retry-After";

    private final RateLimitProperties properties;

    private final RateLimiter login;

    private final RateLimiter token;

    private final RateLimiter publicApi;

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    private final AuditService audit;

    public RateLimitFilter(RateLimitProperties properties, ProblemDetailFactory problems, ObjectMapper json,
                           AuditService audit) {
        this.properties = properties;
        this.login = new RateLimiter(properties.login());
        this.token = new RateLimiter(properties.token());
        this.publicApi = new RateLimiter(properties.publicApi());
        this.problems = problems;
        this.json = json;
        this.audit = audit;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.enabled() || !HttpMethod.POST.matches(request.getMethod()) || ruleFor(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RateLimiter rule = ruleFor(request);
        String key = String.format("%s|%s", request.getRemoteAddr(), request.getServletPath());
        if (rule == null || rule.tryAcquire(key)) {
            chain.doFilter(request, response);
            return;
        }
        long retryAfter = rule.window().toSeconds();
        audit.recordDetached(AuditRecord.of(AuditEventType.RATE_LIMITED).actor(AuditActor.ANONYMOUS)
                .failed(UaaErrors.RATE_LIMITED.code()).detail(request.getServletPath()));
        ProblemDetail problem = problems.create(UaaErrors.RATE_LIMITED, "Too many attempts; try again later.",
                Map.of("retryAfterSeconds", retryAfter), List.of(), problems.traceId());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader(RETRY_AFTER, Long.toString(retryAfter));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), problem);
    }

    private RateLimiter ruleFor(HttpServletRequest request) {
        String path = request.getServletPath();
        if (LOGIN.equals(path)) {
            return login;
        }
        if (TOKEN.equals(path)) {
            return token;
        }
        if (path.startsWith(PUBLIC_PREFIX)) {
            return publicApi;
        }
        return null;
    }

}
