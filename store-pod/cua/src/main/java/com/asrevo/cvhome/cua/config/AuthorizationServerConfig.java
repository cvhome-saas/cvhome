package com.asrevo.cvhome.cua.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    private static final String UNPINNED_ISSUER = """
            cua cannot pin its OAuth2 issuer: com.asrevo.cvhome.pod-info.pod.endpoint.endpoint is not \
            configured. Without it the issuer is derived from the request host, and every token this server \
            mints is rejected downstream.""";

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain authorizationServerSecurity(HttpSecurity http, JwtDecoder jwtDecoder) {
        OAuth2AuthorizationServerConfigurer serverConfigurer = new OAuth2AuthorizationServerConfigurer();
        return http.with(serverConfigurer, configurer -> configurer.oidc(Customizer.withDefaults()))
                .securityMatcher(serverConfigurer.getEndpointsMatcher())
                .authorizeHttpRequests(auth -> auth.requestMatchers(serverConfigurer.getEndpointsMatcher()).authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder)))
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")))
                .build();
    }

    @Bean
    DynamicRegisteredClientRepository registeredClientRepository() {
        return new DynamicRegisteredClientRepository();
    }

    /**
     * Pins the issuer to the pod's endpoint, and refuses to start without one.
     *
     * <p>
     * Pinning is not optional here, and it cannot be replaced by trusting the hosts cua answers on. The browser
     * always reaches cua same-origin, so the request host is the shopper's storefront host — a per-store
     * subdomain, or an arbitrary merchant-owned {@code CUSTOM_DOMAIN}. That set is unbounded and can never be
     * enumerated in a resource server's trust list. One issuer per pod is the only shape that works, and every
     * pod service's trust list is written against exactly that.
     * </p>
     *
     * <p>
     * So the old {@code if (nonNull(...))} guard was the worst of both: without pod info Spring Authorization
     * Server falls back to deriving the issuer per request, which — with Caddy passing {@code Host} through and
     * {@code PathPrefixFilter} honouring {@code X-Forwarded-Prefix} — silently produced exactly the per-store
     * issuer no resource server trusts. Shoppers could sign in and then get rejected by the first API call they
     * made. A cua that cannot pin its issuer mints tokens nothing will accept, so failing to boot is the honest
     * outcome.
     * </p>
     *
     * <p>
     * The endpoint is normalized on the way in because it is operator-entered free text in the pod registry:
     * this is what keeps a pod registered as {@code https://host:443} from advertising an issuer that differs,
     * character for character, from the same pod registered as {@code https://host}.
     * </p>
     */
    @Bean
    AuthorizationServerSettings authorizationServerSettings(PodInfoProperties properties) {
        Pod pod = properties.pod();
        if (Objects.isNull(pod) || Objects.isNull(pod.endpoint()) || Objects.isNull(pod.endpoint().endpoint())) {
            throw new IllegalStateException(UNPINNED_ISSUER);
        }
        String issuer = UrlNormalize.normalizeUri("%s/cua".formatted(pod.endpoint().endpoint()));
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

}
