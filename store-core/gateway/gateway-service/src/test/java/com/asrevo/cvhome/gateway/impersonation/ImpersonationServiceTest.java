package com.asrevo.cvhome.gateway.impersonation;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebSession;

import com.asrevo.cvhome.gateway.errors.ImpersonationAlreadyActiveException;
import com.asrevo.cvhome.gateway.errors.ImpersonationRefusedException;
import com.asrevo.cvhome.gateway.errors.ImpersonationStoreNotTargetsException;
import com.asrevo.cvhome.gateway.errors.ImpersonationUnavailableException;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The swap and its undoing, against an in-memory authorized-client service and stubbed uaa and tenancy.
 *
 * <p>
 * The service the manager reads is the one the swapped client goes into, keyed by principal name: after a start the
 * merchant-named client holds the exchanged token, the operator's own is untouched, and after an end the merchant's
 * is gone. Every failure between exchange and swap leaves the session exactly as it was.
 * </p>
 */
class ImpersonationServiceTest {

    private static final Instant NOW = Instant.parse("2026-04-01T09:30:00Z");

    private static final String TOKEN_URI = "http://uaa.gateway.com:8001/oauth2/token";

    private static final String OPERATOR_ID = "65d8419c-8765-4b8b-a15f-910dce959931";

    private static final String MERCHANT_ID = "60ab49a5-7f06-4b5a-be81-9b30bb6559ae";

    private static final String MERCHANT = "org1-store1-admin";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String REASON = "ticket 42";

    private static final String READ = "read";

    private static final String UAA = "uaa";

    private static final String SUB = "sub";

    private static final String SECURITY_CONTEXT = WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

    private static final String OPERATOR_TOKEN = "operator-token";

    private static final String IMPERSONATION = "console-impersonation";

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private static final String AUTHORIZATION = "Authorization";

    private static final String CONTENT_TYPE = "Content-Type";

    private static final String COMPOSITE = "%s/%s";

    private final ClientRegistration uaaRegistration = ClientRegistration.withRegistrationId(UAA).clientId("web-app")
            .clientSecret("s").authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("http://gateway.com:8000/login/oauth2/code/uaa").authorizationUri("http://uaa/oauth2/authorize")
            .tokenUri(TOKEN_URI).build();

    private final ClientRegistration impersonationRegistration = ClientRegistration.withRegistrationId(IMPERSONATION)
            .clientId(IMPERSONATION).clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.TOKEN_EXCHANGE).tokenUri(TOKEN_URI).build();

    private final InMemoryReactiveClientRegistrationRepository registrations =
            new InMemoryReactiveClientRegistrationRepository(uaaRegistration, impersonationRegistration);

    private final ReactiveOAuth2AuthorizedClientService clients = new InMemoryReactiveOAuth2AuthorizedClientService(registrations);

    private final ReactiveOAuth2AuthorizedClientManager manager = mock(ReactiveOAuth2AuthorizedClientManager.class);

    private final ServiceUrlBuilder urls = mock(ServiceUrlBuilder.class);

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    private final AtomicInteger revocations = new AtomicInteger();

    private final AtomicInteger probes = new AtomicInteger();

    private HttpStatus uaaStatus = HttpStatus.OK;

    private HttpStatus tenancyStatus = HttpStatus.OK;

    private ImpersonationService service;

    private final OAuth2AuthenticationToken operator = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(SUPER_ADMIN)), Map.of(SUB, OPERATOR_ID), SUB),
            List.of(new SimpleGrantedAuthority(SUPER_ADMIN)), UAA);

    private final MockServerWebExchange exchange =
            MockServerWebExchange.from(MockServerHttpRequest.post("/api/v1/impersonation").build());

    {
        when(urls.getServiceUrl("tenancy")).thenReturn("lb://tenancy");
        OAuth2AuthorizedClient operatorClient = new OAuth2AuthorizedClient(uaaRegistration, OPERATOR_ID,
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, OPERATOR_TOKEN, NOW, NOW.plus(Duration.ofMinutes(10))));
        when(manager.authorize(any())).thenReturn(Mono.just(operatorClient));
        clients.saveAuthorizedClient(operatorClient, operator).block();
        WebSession session = exchange.getSession().block();
        session.start();
        session.getAttributes().put(SECURITY_CONTEXT, new SecurityContextImpl(operator));
        service = new ImpersonationService(manager, clients, registrations, stub(this::uaa), stub(this::tenancy), urls, clock);
    }

    private static WebClient stub(Function<ClientRequest, Mono<ClientResponse>> answer) {
        return WebClient.builder().exchangeFunction(answer::apply).build();
    }

    /** The exchanged JWT: unsigned, because the gateway trusts what uaa hands it over the client channel. */
    private static String jwt(Instant exp) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8));
        String payload = String.format("{\"sub\":\"%s\",\"uid\":\"%s\",\"iat\":%d,\"exp\":%d,\"roles\":[\"STORE_MODERATOR\"],"
                        + "\"store\":\"%s\",\"act\":{\"sub\":\"super-admin\"},\"act_mode\":\"read\"}",
                MERCHANT_ID, MERCHANT_ID, NOW.getEpochSecond(), exp.getEpochSecond(), STORE);
        String body = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return String.format("%s.%s.sig", header, body);
    }

    private Mono<ClientResponse> uaa(ClientRequest request) {
        if (request.url().getPath().endsWith("/revoke")) {
            revocations.incrementAndGet();
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        }
        assertThat(request.headers().getFirst(AUTHORIZATION)).startsWith("Basic ");
        if (!uaaStatus.is2xxSuccessful()) {
            return Mono.just(ClientResponse.create(uaaStatus).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"error\":\"access_denied\",\"error_description\":\"This operator may act read-only.\"}").build());
        }
        String body = String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":600,"
                + "\"act_mode\":\"read\",\"acting_as\":\"%s\"}", jwt(NOW.plus(Duration.ofMinutes(10))), MERCHANT);
        return Mono.just(ClientResponse.create(HttpStatus.OK).header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body).build());
    }

    private Mono<ClientResponse> tenancy(ClientRequest request) {
        probes.incrementAndGet();
        assertThat(request.url().toString()).startsWith("lb://tenancy/api/v1/router/store-pod-by-store-id?store=");
        assertThat(request.headers().getFirst(AUTHORIZATION)).startsWith("Bearer ");
        return Mono.just(ClientResponse.create(tenancyStatus).build());
    }

    private StartImpersonation request() {
        return new StartImpersonation(MERCHANT_ID, STORE, READ, REASON);
    }

    private ImpersonationView start() {
        return service.start(exchange, operator, request()).block();
    }

    private SecurityContext sessionContext() {
        return exchange.getSession().block().getAttribute(SECURITY_CONTEXT);
    }

    @Test
    void startingSwapsTheSessionsContextAndClientAndKeepsTheOperators() {
        ImpersonationView view = start();

        assertThat(view.actingAs()).isEqualTo(MERCHANT);
        assertThat(view.storeId()).isEqualTo(STORE);
        assertThat(view.mode()).isEqualTo(READ);
        assertThat(view.reason()).isEqualTo(REASON);
        assertThat(view.expiresAt()).isEqualTo(NOW.plus(Duration.ofMinutes(10)));
        assertThat(probes).hasValue(1);

        // The session's context is now the merchant, with the exchanged token's authorities...
        OAuth2AuthenticationToken acting = (OAuth2AuthenticationToken) sessionContext().getAuthentication();
        assertThat(acting.getAuthorities()).extracting("authority").contains("ROLE_STORE_MODERATOR").doesNotContain(SUPER_ADMIN);
        assertThat(acting.getPrincipal().<String>getAttribute(SUB)).isEqualTo(MERCHANT_ID);
        assertThat(acting.getPrincipal().<String>getAttribute("preferred_username")).isEqualTo(MERCHANT);
        // ...named after target and operator together, never after the merchant alone.
        assertThat(acting.getName()).isEqualTo(String.format(COMPOSITE, MERCHANT_ID, OPERATOR_ID));
        // ...and the client the relay reads for that name carries the exchanged token, while the operator's stands.
        OAuth2AuthorizedClient relayed = clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, acting.getName()).block();
        assertThat(relayed.getAccessToken().getTokenValue()).startsWith("eyJ");
        assertThat(relayed.getRefreshToken()).isNull();
        assertThat(clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, OPERATOR_ID).block().getAccessToken().getTokenValue())
                .isEqualTo(OPERATOR_TOKEN);
        assertThat(service.current(exchange).block()).isEqualTo(view);
    }

    @Test
    void endingRestoresTheOperatorRemovesTheMerchantsClientAndRevokes() {
        start();

        service.end(exchange).block();

        assertThat(sessionContext().getAuthentication()).isSameAs(operator);
        assertThat(clients.loadAuthorizedClient(UAA, String.format(COMPOSITE, MERCHANT_ID, OPERATOR_ID)).blockOptional()).isEmpty();
        assertThat(clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, OPERATOR_ID).block()).isNotNull();
        assertThat(revocations).hasValue(1);
        assertThat(service.current(exchange).blockOptional()).isEmpty();
        // Idempotent: ending a session that is itself is nothing.
        service.end(exchange).block();
        assertThat(revocations).hasValue(1);
    }

    @Test
    void expiryRestoresTheOperatorOnlyOnceTheCeilingHasPassed() {
        start();

        service.expireIfDue(exchange).block();
        assertThat(sessionContext().getAuthentication()).isNotSameAs(operator);

        ImpersonationService later = new ImpersonationService(manager, clients, registrations, stub(this::uaa),
                stub(this::tenancy), urls, Clock.fixed(NOW.plus(Duration.ofMinutes(11)), ZoneOffset.UTC));
        later.expireIfDue(exchange).block();

        assertThat(sessionContext().getAuthentication()).isSameAs(operator);
        assertThat(revocations).hasValue(1);
    }

    @Test
    void logoutGetsTheOperatorBackAndEndsTheImpersonation() {
        start();

        StepVerifier.create(service.originalOf(exchange)).expectNext(operator).verifyComplete();

        assertThat(service.current(exchange).blockOptional()).isEmpty();
        StepVerifier.create(service.originalOf(exchange)).verifyComplete();
    }

    @Test
    void aSecondStartIsRefusedUntilTheFirstIsEnded() {
        start();

        StepVerifier.create(service.start(exchange, operator, request()))
                .expectError(ImpersonationAlreadyActiveException.class).verify();
    }

    @Test
    void uaasRefusalIsForbiddenAndLeavesTheSessionUntouched() {
        uaaStatus = HttpStatus.BAD_REQUEST;

        StepVerifier.create(service.start(exchange, operator, request()))
                .expectErrorSatisfies(e -> assertThat(e).isInstanceOf(ImpersonationRefusedException.class)
                        .hasMessageContaining("read-only"))
                .verify();

        assertThat(sessionContext().getAuthentication()).isSameAs(operator);
        assertThat(probes).hasValue(0);
    }

    /** The store check uaa cannot make: tenancy, asked as the merchant, said no — and the token dies with it. */
    @Test
    void aStoreTenancyRefusesIsUnprocessableAndRevokesTheExchangedToken() {
        tenancyStatus = HttpStatus.NOT_FOUND;

        StepVerifier.create(service.start(exchange, operator, request()))
                .expectError(ImpersonationStoreNotTargetsException.class).verify();

        assertThat(revocations).hasValue(1);
        assertThat(sessionContext().getAuthentication()).isSameAs(operator);
        assertThat(service.current(exchange).blockOptional()).isEmpty();
    }

    @Test
    void anUnreachableUaaIsAremoteFailureNotAswap() {
        ImpersonationService broken = new ImpersonationService(manager, clients, registrations,
                stub(r -> Mono.error(new java.net.ConnectException("refused"))), stub(this::tenancy), urls, clock);

        StepVerifier.create(broken.start(exchange, operator, request()))
                .expectError(ImpersonationUnavailableException.class).verify();

        assertThat(sessionContext().getAuthentication()).isSameAs(operator);
    }

    @Test
    void theRevocationEndpointIsTheTokenEndpointsSibling() {
        assertThat(ImpersonationService.revocationUri(impersonationRegistration))
                .isEqualTo("http://uaa.gateway.com:8001/oauth2/revoke");
    }

}
