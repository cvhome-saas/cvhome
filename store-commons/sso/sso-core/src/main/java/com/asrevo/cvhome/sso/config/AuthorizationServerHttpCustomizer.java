package com.asrevo.cvhome.sso.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * A deployment's addition to the authorization-server filter chain.
 *
 * <p>
 * The chain itself is shared, because the protocol is. What differs is small and specific: cua inserts a filter
 * that honours {@code prompt=login} by ending the current session before the chain decides whether the shopper
 * is signed in. Rather than fork the chain for that, a shell contributes one of these.
 * </p>
 */
@FunctionalInterface
public interface AuthorizationServerHttpCustomizer {

    void customize(HttpSecurity http) throws Exception;

}
