package com.asrevo.cvhome.cua.realm;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import com.asrevo.cvhome.cua.config.StorefrontClientRepository;
import com.asrevo.cvhome.sso.realm.RealmRepository;
import com.asrevo.cvhome.sso.realm.RealmResolver;

/**
 * cua's half of the realm seam: a request belongs to the store it arrived for.
 *
 * <p>
 * Supplying this is not optional in {@code MULTI} mode — sso-core refuses to start without it rather than fall
 * back to a single realm, which would put every store's shoppers in one pool.
 * </p>
 */
@Configuration
public class CuaRealmConfig {

    @Bean
    RealmResolver realmResolver() {
        return new StoreRealmResolver();
    }

    /**
     * Replaces sso-core's JDBC-backed client registry. uaa's clients are rows an administrator manages; a
     * storefront's is derived from the store, because its valid redirect URIs span every domain and language the
     * store is reached on.
     */
    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate, RealmRepository realms) {
        return new StorefrontClientRepository(new JdbcRegisteredClientRepository(jdbcTemplate), realms);
    }

}
