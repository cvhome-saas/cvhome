package com.asrevo.cvhome.sso.realm;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Wires the realm into the request and into Hibernate.
 *
 * <p>
 * The {@link RealmResolver} is {@link ConditionalOnMissingBean} because it is one of the two seams a deployment
 * fills: uaa gets the {@link FixedRealmResolver} below by saying nothing, and cua supplies a host-based one.
 * </p>
 */
@Configuration
@EnableConfigurationProperties(SsoRealmProperties.class)
public class RealmConfig {

    /**
     * The default seam filler, for {@code SINGLE} deployments. A {@code MULTI} deployment that fails to supply its
     * own resolver would otherwise serve every store from one realm, so it is refused at startup instead.
     */
    @Bean
    @ConditionalOnMissingBean(RealmResolver.class)
    RealmResolver realmResolver(SsoRealmProperties properties) {
        if (!properties.single()) {
            throw new IllegalStateException("""
                    com.asrevo.cvhome.sso.realm.mode is MULTI but no RealmResolver bean was supplied. A \
                    multi-realm deployment has to say how a request maps to a store; without one every store \
                    would share a single user pool.""");
        }
        return new FixedRealmResolver(properties.fixedRealm());
    }

    @Bean
    SsoTenantIdentifierResolver ssoTenantIdentifierResolver(SsoRealmProperties properties) {
        return new SsoTenantIdentifierResolver(properties);
    }

    /**
     * Hibernate reads the tenant identifier from this on every query and every insert. Registered explicitly
     * rather than left to bean detection so that a refactor cannot quietly turn realm filtering off.
     */
    @Bean
    HibernatePropertiesCustomizer realmTenantIdentifier(SsoTenantIdentifierResolver resolver) {
        return properties -> properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }

    /**
     * Ahead of Spring Security: authenticating a username is only meaningful once the realm is known, and in
     * {@code MULTI} mode the same username exists in many realms.
     */
    @Bean
    FilterRegistrationBean<RealmFilter> realmFilter(RealmResolver resolver) {
        FilterRegistrationBean<RealmFilter> registration = new FilterRegistrationBean<>(new RealmFilter(resolver));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
