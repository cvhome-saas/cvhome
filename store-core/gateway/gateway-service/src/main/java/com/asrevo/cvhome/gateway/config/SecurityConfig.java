package com.asrevo.cvhome.gateway.config;

import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.util.StringUtils;

import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;
import com.nimbusds.jwt.SignedJWT;

import lombok.SneakyThrows;

import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * The gateway's one filter chain.
 *
 * <p>
 * A relay first: every exchange is permitted and the backend judges the bearer token the session carries, so CSRF
 * is off (see the session cookie in application.yml for what closes the cross-site case instead) and no method
 * security is enabled. The one exception is the actuator, which is the gateway's own surface and a platform
 * operator's read.
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    static final String HEALTH = "/actuator/health";

    static final String ACTUATOR = "/actuator/**";

    static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private static final String STORE_CORE_SCOPE = "SCOPE_STORE_CORE";

    private static final String STORE_CORE_SCOPE_LOWER = "SCOPE_store_core";

    private final RedirectingServerAuthenticationSuccessHandler redirectingServerAuthenticationSuccessHandler =
            new RedirectingServerAuthenticationSuccessHandler();

    @SneakyThrows
    private static Set<GrantedAuthority> extractAuthority(OAuth2AccessToken accessToken) {
        SignedJWT parsed = SignedJWT.parse(accessToken.getTokenValue());
        Map<String, Object> claims = parsed.getPayload().toJSONObject();
        return UaaJwtGrantedAuthoritiesConverter.getGrantedAuthorities(claims);
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        CapturingServerOAuth2AuthorizationRequestResolver customAuthorizationRequestResolver =
                new CapturingServerOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository);

        return http.authorizeExchange(it -> it
                        // The ALB and lcl probe health anonymously and read only the status.
                        .pathMatchers(HEALTH).permitAll()
                        // The rest of the actuator is a platform-operator read. The gateway's own actuator was public
                        // (authorization audit, A1): common-config.yml narrows what is mapped, this narrows who may
                        // read whatever a debugging session widens it to. The OIDC login carries uaa's roles as
                        // ROLE_* authorities (extractAuthority); the store_core scope forms are the spelling a
                        // platform client would carry, in both cases the converter emits.
                        .pathMatchers(ACTUATOR).hasAnyAuthority(SUPER_ADMIN, STORE_CORE_SCOPE, STORE_CORE_SCOPE_LOWER)
                        // Everything else is a relay: the session's bearer token, or none, is what the backend
                        // judges. Session-bound endpoints on this gateway gate themselves (ImpersonationController).
                        .anyExchange().permitAll())
                // The only exchanges that can be refused are the actuator's, and they are read by an operator's tools,
                // not a browser: a 401, not a redirect into the login flow.
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .oauth2Login(oauth2 -> oauth2
                                // Use the custom resolver here
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                                .authenticationSuccessHandler(this.redirectingServerAuthenticationSuccessHandler)
                        // You can also customize authenticationSuccessHandler or
                        // authenticationFailureHandler if needed
                )
                .oauth2Client(withDefaults())
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable
                        // .csrfTokenRequestHandler(new
                        // ServerCsrfTokenRequestAttributeHandler())
                        // .csrfTokenRepository(tokenRepository)
                        //
                        // .requireCsrfProtectionMatcher(SecurityConfig::matches)
                )
                /*
                 * .cors(httpSecurityCorsConfigurer ->
                 * httpSecurityCorsConfigurer.configurationSource(request ->
                 * buildReactivCorsConfiguration() ) )
                 */
                .build();
    }

    @Bean
    public CookieServerCsrfTokenRepository cookieServerCsrfTokenRepository() {
        return CookieServerCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
        return userRequest -> delegate.loadUser(userRequest).flatMap(oidcUser -> {
            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            Set<GrantedAuthority> r = extractAuthority(accessToken);
            ClientRegistration.ProviderDetails providerDetails = userRequest.getClientRegistration()
                    .getProviderDetails();
            String userNameAttributeName = providerDetails.getUserInfoEndpoint().getUserNameAttributeName();
            if (StringUtils.hasText(userNameAttributeName)) {
                oidcUser = new DefaultOidcUser(r, oidcUser.getIdToken(), oidcUser.getUserInfo(), userNameAttributeName);
            } else {
                oidcUser = new DefaultOidcUser(r, oidcUser.getIdToken(), oidcUser.getUserInfo());
            }
            return Mono.just(oidcUser);
        });
    }

}
