package com.asrevo.cvhome.sso.realm;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.commons.domain.RealmId;

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
 */
public class RealmFilter extends OncePerRequestFilter {

    private final RealmResolver resolver;

    public RealmFilter(RealmResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RealmId realm = resolver.resolve(request);
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

}
