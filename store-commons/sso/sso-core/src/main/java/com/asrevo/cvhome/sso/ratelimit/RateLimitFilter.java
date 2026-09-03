package com.asrevo.cvhome.sso.ratelimit;

import java.io.IOException;
import java.time.Duration;
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

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;
import com.asrevo.cvhome.sso.audit.AuditActor;
import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.realm.RealmContext;
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
 *
 * <p>
 * It runs after the realm filter, which is what lets the attempt be counted against the store it was aimed at as
 * well as against the address it came from. See {@link RateLimitProperties} for why both.
 * </p>
 */
public class RateLimitFilter extends OncePerRequestFilter {

    static final String LOGIN = "/login";

    static final String TOKEN = "/oauth2/token";

    static final String PUBLIC_PREFIX = "/api/v1/public/";

    static final String RETRY_AFTER = "Retry-After";

    private final RateLimitProperties properties;

    private final Buckets login;

    private final Buckets token;

    private final Buckets publicApi;

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    private final AuditService audit;

    public RateLimitFilter(RateLimitProperties properties, ProblemDetailFactory problems, ObjectMapper json,
                           AuditService audit) {
        this.properties = properties;
        this.login = Buckets.of(properties.login(), properties.spread());
        this.token = Buckets.of(properties.token(), properties.spread());
        this.publicApi = Buckets.of(properties.publicApi(), properties.spread());
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
        Buckets rule = ruleFor(request);
        if (rule == null || rule.tryAcquire(realm(), request.getRemoteAddr(), request.getServletPath())) {
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

    /** The realm the edge resolved, or a placeholder — an unrealmed POST is still counted, just not per store. */
    private static String realm() {
        return RealmContext.current().map(RealmId::getId).orElse("-");
    }

    private Buckets ruleFor(HttpServletRequest request) {
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

    /**
     * One rule's two counters.
     *
     * <p>
     * The realm's is checked first and spent whether or not the address's then refuses, because the attempt was
     * made either way; a fixed window is a brake on guessing, not a ledger.
     * </p>
     */
    private record Buckets(RateLimiter perRealm, RateLimiter perAddress) {

        static Buckets of(RateLimitProperties.Rule rule, int spread) {
            return new Buckets(new RateLimiter(rule),
                    new RateLimiter(new RateLimitProperties.Rule(rule.limit() * spread, rule.window())));
        }

        boolean tryAcquire(String realm, String address, String path) {
            boolean realmHasRoom = perRealm.tryAcquire(String.format("%s|%s|%s", realm, address, path));
            boolean addressHasRoom = perAddress.tryAcquire(String.format("%s|%s", address, path));
            return realmHasRoom && addressHasRoom;
        }

        Duration window() {
            return perRealm.window();
        }
    }

}
