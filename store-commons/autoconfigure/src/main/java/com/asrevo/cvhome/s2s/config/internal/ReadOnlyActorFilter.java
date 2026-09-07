package com.asrevo.cvhome.s2s.config.internal;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.errors.CommonErrors;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * What makes a read-only impersonation read-only.
 *
 * <p>
 * The exchanged token carries the merchant's own roles — so everything the merchant can see, the operator can see —
 * and {@code act_mode=read}. This filter is the other half: for such a token, every unsafe method is refused with a
 * problem detail, except the paths {@link ReadOnlyActorProperties} names as reads that happen to travel as
 * {@code POST}. Refused here rather than in the permission evaluator because the permission tokens cannot tell a
 * read from a write — {@code STORE-POD.CATALOG.*} guards the list and the save alike — and the method can.
 * </p>
 *
 * <p>
 * Runs after the security chain, in the servlet chain, so the principal is already resolved; only a JWT principal
 * with the claim is affected, which is why it can sit on every service without knowing which of them it is on.
 * </p>
 */
@Slf4j
public class ReadOnlyActorFilter extends OncePerRequestFilter {

    /** The claim an impersonated token carries: {@code read} or {@code write}. */
    public static final String ACT_MODE = "act_mode";

    static final String READ = "read";

    private static final Set<String> SAFE = Set.of(HttpMethod.GET.name(), HttpMethod.HEAD.name(),
            HttpMethod.OPTIONS.name());

    private final AntPathMatcher paths = new AntPathMatcher();

    private final List<String> allowedWrites;

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    public ReadOnlyActorFilter(ReadOnlyActorProperties properties, ProblemDetailFactory problems, ObjectMapper json) {
        this.allowedWrites = properties.allowedWrites();
        this.problems = problems;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SAFE.contains(request.getMethod()) || !isReadOnlyActor() || allowed(request)) {
            chain.doFilter(request, response);
            return;
        }
        String path = pathOf(request);
        log.info("Refusing {} {} for a read-only impersonation", request.getMethod(), path);
        ProblemDetail problem = problems.create(CommonErrors.READ_ONLY_SESSION,
                "This session is acting as a merchant read-only; nothing can be changed.", Map.of("path", path),
                List.of(), problems.traceId());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), problem);
    }

    private static boolean isReadOnlyActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof Jwt jwt
                && READ.equals(jwt.getClaimAsString(ACT_MODE));
    }

    private boolean allowed(HttpServletRequest request) {
        String path = pathOf(request);
        return allowedWrites.stream().anyMatch(pattern -> paths.match(pattern, path));
    }

    private static String pathOf(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        return context != null && !context.isEmpty() && uri.startsWith(context) ? uri.substring(context.length()) : uri;
    }

}
