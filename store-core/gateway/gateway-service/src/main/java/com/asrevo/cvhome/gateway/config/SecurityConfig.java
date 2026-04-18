package com.asrevo.cvhome.gateway.config;

import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import com.asrevo.cvhome.s2s.jwt.UaaJwtGrantedAuthoritiesConverter;
import com.nimbusds.jwt.SignedJWT;

import lombok.SneakyThrows;

import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

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

        return http.authorizeExchange(it -> it.anyExchange().permitAll())
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
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("STORE-CORE-GATEWAY-JSESSIONID");
        resolver.addCookieInitializer((builder) -> builder.path("/"));
        return resolver;
    }

    @Bean
    public ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
        return (userRequest) -> delegate.loadUser(userRequest).flatMap((oidcUser) -> {
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
