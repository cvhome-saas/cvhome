package com.asrevo.cvhome.uaa.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.config.AuthorizationServerHttpCustomizer;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.token.ImpersonationExchangeConverter;
import com.asrevo.cvhome.sso.token.ImpersonationExchangeProvider;

/**
 * Turns the impersonation grant on — for uaa, and for uaa only.
 *
 * <p>
 * The grant lives in sso-core, but it is registered here, in the shell, because it must never reach cua: a
 * shopper realm has no operators and no merchants to act as, and a token endpoint that answered
 * {@code grant_type=token-exchange} there would be an attack surface with no feature behind it.
 * </p>
 *
 * <p>
 * The converter is added through {@code accessTokenRequestConverter}, which the configurer places <em>ahead</em> of
 * its defaults, so ours is consulted before Spring's own token-exchange converter — the built-in one cannot express
 * "act as account X" and would reject the request as malformed. The provider is built inside the
 * {@code authenticationProviders} callback rather than up front, because that is the first moment the token
 * generator exists as a shared object: the configurer creates it while building its default providers, and the
 * callback runs right after.
 * </p>
 */
@Configuration
public class ImpersonationGrantCustomizer {

    @Bean
    AuthorizationServerHttpCustomizer impersonationGrant(OAuth2AuthorizationService authorizations, UserRepository users,
                                                         RoleRepository roles, AuditService audit, Clock clock) {
        return http -> http.getConfigurer(OAuth2AuthorizationServerConfigurer.class).tokenEndpoint(token -> token
                .accessTokenRequestConverter(new ImpersonationExchangeConverter())
                .authenticationProviders(providers -> {
                    @SuppressWarnings("unchecked")
                    OAuth2TokenGenerator<? extends org.springframework.security.oauth2.core.OAuth2Token> generator =
                            http.getSharedObject(OAuth2TokenGenerator.class);
                    providers.add(0, new ImpersonationExchangeProvider(authorizations, users, roles, generator, audit,
                            clock));
                }));
    }

}
