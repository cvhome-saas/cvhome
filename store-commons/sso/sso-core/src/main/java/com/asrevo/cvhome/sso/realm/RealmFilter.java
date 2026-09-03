package com.asrevo.cvhome.sso.realm;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

/**
 * Enters the request's realm before anything else runs, and leaves it on the way out.
 *
 * <p>
 * It is ordered ahead of Spring Security because authentication itself is realm-scoped: looking a user up by
 * username is only meaningful once the realm is known, and in {@code MULTI} mode the same username exists in many
 * of them.
 * </p>
 *
 * <p>
 * A request the resolver does not place in any realm — the JWKS document, the discovery endpoint — proceeds with
 * no realm entered rather than a default one. Those endpoints are identical for every realm the deployment
 * serves, and anything realm-scoped that reached them would fail loudly instead of quietly answering for the
 * wrong tenant.
 * </p>
 *
 * <p>
 * A resolver that refuses outright renders its own problem body here. A filter throws outside the reach of the
 * {@code @ControllerAdvice}, so without this the refusal reached the client as a bare 500 — which is what the
 * first end-to-end run of a cross-store request produced.
 * </p>
 */
public class RealmFilter extends OncePerRequestFilter {

    private final RealmResolver resolver;

    private final ProblemDetailFactory problems;

    private final ObjectMapper json;

    public RealmFilter(RealmResolver resolver, ProblemDetailFactory problems, ObjectMapper json) {
        this.resolver = resolver;
        this.problems = problems;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RealmId realm;
        try {
            realm = resolver.resolve(request);
        } catch (RealmResolutionException refused) {
            refuse(response, refused);
            return;
        }
        if (realm == null) {
            chain.doFilter(request, response);
            return;
        }
        try {
            RealmContext.enter(realm);
            chain.doFilter(request, response);
        } finally {
            RealmContext.leave();
        }
    }

    private void refuse(HttpServletResponse response, RealmResolutionException refused) throws IOException {
        ProblemDetail problem = problems.create(refused.code(), refused.getMessage(), Map.of(), List.of(),
                problems.traceId());
        response.setStatus(refused.code().category().httpStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        json.writeValue(response.getOutputStream(), problem);
    }

}
