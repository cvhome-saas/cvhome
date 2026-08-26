package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import com.asrevo.cvhome.s2s.jwt.IssuerRegistry;
import com.asrevo.cvhome.s2s.jwt.RealmAwareJwtGrantedAuthoritiesConverter;
import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;

/**
 * How a token's claims become authorities, for every service at once.
 *
 * <p>
 * Eleven services used to declare this bean identically, which is how the claim-to-authority mapping came to be a
 * thing each of them could drift on. It belongs here instead: it is the same question everywhere, and now that
 * the answer depends on which realm signed the token it needs the registry injected, which a hand-rolled copy in
 * a service cannot do.
 * </p>
 *
 * <p>
 * Registering it is not optional for an authorization server either, which is easy to assume. Spring's default
 * converter maps only {@code scope} to {@code SCOPE_*}; both uaa and cua <em>mint</em> a {@code roles} claim and
 * neither read it back without this. In uaa that meant {@code hasRole('SUPER_ADMIN')} — on its own filter chain
 * and on every method of {@code AdminUserController} — could be satisfied by no user token in existence. Only
 * the {@code SCOPE_super_admin} half worked, and only for the client-credentials token the admin SDK uses, which
 * is why the gap survived: that API had one caller and it came in the one way that worked.
 * </p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class JwtAuthenticationConverterConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JwtAuthenticationConverter jwtAuthenticationConverter(ObjectProvider<IssuerRegistry> registry) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        IssuerRegistry issuerRegistry = registry.getIfAvailable();
        // Without a registry there is nothing to attribute a token to, so fall back to the realm-blind mapping
        // rather than failing: a service may legitimately verify tokens through Boot's single-issuer support.
        converter.setJwtGrantedAuthoritiesConverter(issuerRegistry == null ? new UaaJwtGrantedAuthoritiesConverter()
                : new RealmAwareJwtGrantedAuthoritiesConverter(issuerRegistry));
        return converter;
    }

}
