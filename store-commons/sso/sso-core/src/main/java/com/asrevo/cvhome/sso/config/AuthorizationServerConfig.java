package com.asrevo.cvhome.sso.config;

import java.time.Clock;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.RequestCache;

import com.asrevo.cvhome.sso.client.EnabledAwareRegisteredClientRepository;
import com.asrevo.cvhome.sso.client.GraceAwareClientSecretAuthenticationProvider;
import com.asrevo.cvhome.sso.repo.ClientExtensionRepository;
import com.asrevo.cvhome.sso.repo.ClientSecretHistoryRepository;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * The authorization server proper: the protocol endpoints, where clients and authorizations live, and the issuer.
 *
 * <p>
 * <strong>Authorizations are in the database.</strong> Without an {@code OAuth2AuthorizationService} bean the server
 * kept every issued token in memory, so a restart invalidated every refresh token and nothing could ever be revoked —
 * disabling a user left their tokens working until they expired. {@code uaa.oauth2_authorization} is now real.
 * </p>
 *
 * <p>
 * <strong>Clients can be switched off, and a rotated secret keeps working for a while.</strong> The registry the
 * protocol endpoints see hides a disabled client from {@code findByClientId}, and the client-secret provider is uaa's
 * grace-aware one, which lets a secret rotated out within the realm's grace window still authenticate.
 * </p>
 *
 * <p>
 * <strong>The issuer is pinned</strong> to uaa's entry in the service registry. Left unset, it is derived from each
 * request's {@code Host} header — and with forwarded headers honoured, from whatever a proxy says — so a token's
 * {@code iss} depended on the path the request took. cua pins its issuer the same way and refuses to boot without one.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurity(HttpSecurity http, RequestCache requestCache,
                                                    AuthenticationEntryPoint entryPoint,
                                                    ObjectProvider<AuthorizationServerHttpCustomizer> customizers,
                                                    RegisteredClientRepository clients, OAuth2AuthorizationService authorizations,
                                                    PasswordEncoder encoder, ClientSecretHistoryRepository history, Clock clock)
            throws Exception {
        // Built here rather than as a bean: a lone AuthenticationProvider bean becomes the global manager's provider.
        var graceAware = new GraceAwareClientSecretAuthenticationProvider(clients, authorizations, encoder, history, clock);
        OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.with(serverConfigurer, configurer -> configurer.oidc(Customizer.withDefaults())
                        .clientAuthentication(clientAuth -> clientAuth.authenticationProviders(providers ->
                                providers.replaceAll(p -> p instanceof ClientSecretAuthenticationProvider ? graceAware : p))))
                .securityMatcher(serverConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(auth -> auth.requestMatchers(serverConfigurer.getEndpointsMatcher()).authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .requestCache(cache -> cache.requestCache(requestCache))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));
        for (AuthorizationServerHttpCustomizer customizer : customizers.orderedStream().toList()) {
            customizer.customize(http);
        }
        return http.build();
    }

    /**
     * What an unauthenticated {@code /oauth2/authorize} is answered with. uaa serves its own sign-in page; cua
     * renders no HTML and hands the shopper to their storefront's, so it replaces this bean.
     */
    @Bean
    @ConditionalOnMissingBean(name = "authorizationServerEntryPoint")
    AuthenticationEntryPoint authorizationServerEntryPoint() {
        return new LoginUrlAuthenticationEntryPoint("/login");
    }

    /**
     * Clients as rows an administrator manages, which is uaa's shape. cua replaces this: a storefront's client is
     * derived from the store, because its valid redirect URIs span every domain and language the store is reached
     * on and could never be enumerated in a column.
     */
    @Bean
    @ConditionalOnMissingBean(RegisteredClientRepository.class)
    RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate, ClientExtensionRepository extensions) {
        return new EnabledAwareRegisteredClientRepository(new JdbcRegisteredClientRepository(jdbcTemplate), extensions);
    }

    @Bean
    OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, clients);
    }

    @Bean
    OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                  RegisteredClientRepository clients) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, clients);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(IssuerPin pin) {
        return AuthorizationServerSettings.builder().issuer(pin.issuer()).build();
    }

    @Bean
    @Primary
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
