package com.asrevo.cvhome.s2s.config.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

public class KeycloakLogoutSuccessHandler  {
    private final ReactiveClientRegistrationRepository registrationRepository;
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
    public static final String DEFAULT_LOGOUT_SUCCESS_URL = "/login?logout";

    private final URI logoutSuccessUrl = URI.create(DEFAULT_LOGOUT_SUCCESS_URL);


    public KeycloakLogoutSuccessHandler(ReactiveClientRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    public Mono<Void> onLogoutSuccess(ServerWebExchange exchange, Authentication authentication) {
        try {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            ClientRegistration clientRegistration = registrationRepository.findByRegistrationId(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId()).block();
            String logoutUrl = extractLogoutUrl(exchange, clientRegistration);
            URI logoutSuccessUrl = URI.create(logoutUrl);
            return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
        } catch (Exception e) {
            return this.redirectStrategy.sendRedirect(exchange, logoutSuccessUrl);
        }

    }

    private static String extractLogoutUrl(ServerWebExchange exchange, ClientRegistration clientRegistration) {
        ClientRegistration.ProviderDetails providerDetails = clientRegistration.getProviderDetails();
        ServerHttpRequest request = exchange.getRequest();
        String redirectPath = Optional.ofNullable(exchange.getRequest().getQueryParams().getFirst("redirect_path")).orElse("");
        String redirectUri = request.getURI().getScheme() + "://" + request.getURI().getAuthority()+"/"+redirectPath;
        return providerDetails.getAuthorizationUri().replace("protocol/openid-connect/auth", "protocol/openid-connect/logout?post_logout_redirect_uri=" + redirectUri + "&client_id=" + clientRegistration.getClientId());
    }
}
