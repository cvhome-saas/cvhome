package com.asrevo.cvhome.cua.realm;

import java.io.IOException;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.asrevo.cvhome.sso.realm.RealmContext;
import com.asrevo.cvhome.sso.realm.RealmRegistry;

import lombok.RequiredArgsConstructor;

/**
 * Registers a store's realm the first time a shopper arrives on its storefront.
 *
 * <p>
 * A store gets its realm without anything having to provision it, because the pod edge has already done the work
 * that matters: it resolved the storefront host to a store before the request reached cua, and set
 * {@code Store-Id} from that. A request carrying that header is a request for a store that exists.
 * </p>
 *
 * <p>
 * That is also why the header, and nothing else, is allowed to create one. A store id out of {@code ?store=} or a
 * login form is whatever the caller typed; those may address a realm but may not bring one into being.
 * </p>
 */
@RequiredArgsConstructor
public class EdgeVerifiedRealmFilter extends OncePerRequestFilter {

    private final RealmRegistry realms;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (Objects.nonNull(request.getHeader(StoreRealmResolver.STORE_HEADER))) {
            RealmContext.current().ifPresent(realms::ensure);
        }
        chain.doFilter(request, response);
    }

}
