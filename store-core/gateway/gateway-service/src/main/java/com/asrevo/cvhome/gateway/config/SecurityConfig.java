package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.s2s.jwt.KeyClockJwtGrantedAuthoritiesConverter;
import com.nimbusds.jwt.SignedJWT;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.match;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult.notMatch;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private final RedirectingServerAuthenticationSuccessHandler redirectingServerAuthenticationSuccessHandler = new RedirectingServerAuthenticationSuccessHandler();
    private static Mono<ServerWebExchangeMatcher.MatchResult> matches(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // @formatter:off
        return (
                request.getMethod() != HttpMethod.GET &&
                        !request.getPath().toString().startsWith("/auth") &&
                        !request.getPath().toString().startsWith("/realms") &&
                        !request.getPath().toString().startsWith("/resources") &&
                        !request.getURI().getHost().startsWith("auth.")
        ) ? match() : notMatch();
        // @formatter:on
    }

    @SneakyThrows
    private static Set<GrantedAuthority> extractAuthority(OAuth2AccessToken accessToken) {
        SignedJWT parsed = SignedJWT.parse(accessToken.getTokenValue());
        Map<String, List<String>> realmAccess = (Map<String, List<String>>) parsed.getPayload().toJSONObject().get("realm_access");
        return KeyClockJwtGrantedAuthoritiesConverter.getRolesAuthorities(realmAccess)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        // @formatter:off
        CapturingServerOAuth2AuthorizationRequestResolver customAuthorizationRequestResolver =
                new CapturingServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository);

        return http.authorizeExchange(it ->
                        it.anyExchange().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                                // Use the custom resolver here
                                .authorizationRequestResolver(customAuthorizationRequestResolver)
                                .authenticationSuccessHandler(this.redirectingServerAuthenticationSuccessHandler)
                        // You can also customize authenticationSuccessHandler or authenticationFailureHandler if needed
                )
                .oauth2Client(withDefaults())
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable
//                                .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
//                                .csrfTokenRepository(tokenRepository)
//                                .requireCsrfProtectionMatcher(SecurityConfig::matches)
                )
/*
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(request ->
                                buildReactivCorsConfiguration()
                        )
                )
*/
                .build();
        // @formatter:on
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
        return (userRequest) -> delegate.loadUser(userRequest)
                .flatMap((oidcUser) -> {
                    OAuth2AccessToken accessToken = userRequest.getAccessToken();
                    Set<GrantedAuthority> r = extractAuthority(accessToken);
                    ClientRegistration.ProviderDetails providerDetails = userRequest.getClientRegistration().getProviderDetails();
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
