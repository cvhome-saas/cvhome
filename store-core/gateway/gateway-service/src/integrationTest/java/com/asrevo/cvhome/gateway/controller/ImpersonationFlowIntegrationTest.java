package com.asrevo.cvhome.gateway.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.testsupport.annotations.ReactiveIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * The swap end to end over the real filter chain: a signed-in session starts acting as a merchant, the same session
 * then answers {@code auth/me} as that merchant with the operator behind it, and ending — or the ceiling passing —
 * hands the session back and revokes the exchanged token.
 *
 * <p>
 * uaa and tenancy are stood in for by one throwaway HTTP server: the token endpoint issues a signed JWT for
 * whatever subject is asked, refusing one and back-dating another, and the router probe accepts every store but
 * one. The login itself is mocked at the security layer — a real one needs uaa's browser flow — and the operator's
 * authorized client is planted where a login would have put it, keyed by principal name, so the manager the service
 * refreshes through finds it.
 * </p>
 */
@ReactiveIntegrationTest
class ImpersonationFlowIntegrationTest {

    private static final String IMPERSONATION = "/api/v1/impersonation";

    private static final String ME = "/api/v1/auth/me";

    private static final String COOKIE = "STORE-CORE-GATEWAY-JSESSIONID";

    private static final String UAA = "uaa";

    private static final String SUB = "sub";

    private static final String OPERATOR_ID = "65d8419c-8765-4b8b-a15f-910dce959931";

    private static final String OPERATOR_TOKEN = "operator-token";

    private static final String MERCHANT_ID = "60ab49a5-7f06-4b5a-be81-9b30bb6559ae";

    private static final String MERCHANT = "org1-store1-admin";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String FOREIGN_STORE = "65f023632bc46470c104b77f";

    /** The subject the stubbed token endpoint refuses. */
    private static final String REFUSED = "refused-target";

    /** The subject the stubbed token endpoint issues an already-expired token for. */
    private static final String EXPIRED = "expired-target";

    private static final String READ = "read";

    private static final String REASON = "ticket 42";

    private static final String SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    private static final String COMPOSITE = "%s/%s";

    private static final String JSON = "{}";

    private static final String CLAIMS_SUB = "$.principal.claims.sub";

    private static final String CODE = "$.code";

    private static final String TOKEN_PATH = "/oauth2/token";

    private static final String PREFERRED_USERNAME = "preferred_username";

    private static final String OPERATOR = "super-admin";

    private static final String UID = "uid";

    private static final AtomicInteger REVOCATIONS = new AtomicInteger();

    private static final TestJwtSigner SIGNER = signer();

    private static final HttpServer STUB = stub();

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ReactiveClientRegistrationRepository registrations;

    @Autowired
    private ReactiveOAuth2AuthorizedClientService clients;

    @MockitoBean
    private ReactiveExternalPodService podService;

    @MockitoBean
    private ReactiveExternalEntitlementService entitlementService;

    private WebTestClient client;

    @DynamicPropertySource
    static void pointUaaAndTenancyAtTheStub(DynamicPropertyRegistry registry) {
        String base = String.format("http://localhost:%d", STUB.getAddress().getPort());
        registry.add("spring.security.oauth2.client.provider.uaa.token-uri", () -> String.format("%s%s", base, TOKEN_PATH));
        registry.add("spring.cloud.discovery.client.simple.instances.tenancy[0].uri", () -> base);
    }

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
        OAuth2AuthenticationToken operator = operator();
        OAuth2AuthorizedClient operatorClient = new OAuth2AuthorizedClient(registrations.findByRegistrationId(UAA).block(),
                OPERATOR_ID, new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, OPERATOR_TOKEN, Instant.now(),
                        Instant.now().plusSeconds(600)));
        clients.saveAuthorizedClient(operatorClient, operator).block();
    }

    @Test
    void startingActsAsTheMerchantInThatSessionAndEndingHandsItBack() {
        EntityExchangeResult<byte[]> started = start(MERCHANT_ID, STORE).expectStatus().isOk()
                .expectBody()
                .jsonPath("$.actingAs").isEqualTo(MERCHANT)
                .jsonPath("$.targetId").isEqualTo(MERCHANT_ID)
                .jsonPath("$.storeId").isEqualTo(STORE)
                .jsonPath("$.mode").isEqualTo(READ)
                .jsonPath("$.reason").isEqualTo(REASON)
                .returnResult();
        String session = sessionOf(started);
        String composite = String.format(COMPOSITE, MERCHANT_ID, OPERATOR_ID);

        // The same session — with no mocked login this time — is the merchant: what auth/me tells the console.
        client.get().uri(ME).cookie(COOKIE, session).exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath(CLAIMS_SUB).isEqualTo(MERCHANT_ID)
                .jsonPath("$.principal.name").isEqualTo(composite)
                .jsonPath("$.principal.preferredUsername").isEqualTo(MERCHANT)
                .jsonPath("$.authorities[*].authority").value(authorities -> assertThat(authorities.toString())
                        .contains("ROLE_STORE_ADMIN").doesNotContain(SUPER_ADMIN))
                .jsonPath("$.impersonation.actingAs").isEqualTo(MERCHANT)
                .jsonPath("$.impersonation.mode").isEqualTo(READ);
        // ...and the client tokenRelay() reads for it carries the exchanged token, while the operator's stands.
        OAuth2AuthorizedClient relayed = clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, composite).block();
        assertThat(relayed.getAccessToken().getTokenValue()).startsWith("eyJ");
        assertThat(clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, OPERATOR_ID).block().getAccessToken()
                .getTokenValue()).isEqualTo(OPERATOR_TOKEN);

        // A second start on a session already acting is a conflict, never a chain.
        client.mutateWith(mockAuthentication(operator())).post().uri(IMPERSONATION).cookie(COOKIE, session)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body(MERCHANT_ID, STORE)).exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().jsonPath(CODE).isEqualTo("GATEWAY.IMPERSONATION.ALREADY_ACTIVE");

        int revoked = REVOCATIONS.get();
        client.delete().uri(IMPERSONATION).cookie(COOKIE, session).exchange().expectStatus().isNoContent();

        assertThat(REVOCATIONS).hasValue(revoked + 1);
        client.get().uri(IMPERSONATION).cookie(COOKIE, session).exchange().expectStatus().isNoContent();
        client.get().uri(ME).cookie(COOKIE, session).exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath(CLAIMS_SUB).isEqualTo(OPERATOR_ID)
                .jsonPath("$.impersonation").isEmpty();
        assertThat(clients.loadAuthorizedClient(UAA, composite).blockOptional()).isEmpty();
    }

    @Test
    void aRefusalFromUaaIsForbiddenAndLeavesTheSessionAsItWas() {
        start(REFUSED, STORE).expectStatus().isForbidden()
                .expectBody().jsonPath(CODE).isEqualTo("GATEWAY.IMPERSONATION.REFUSED");

        assertThat(clients.loadAuthorizedClient(UAA, String.format(COMPOSITE, REFUSED, OPERATOR_ID)).blockOptional()).isEmpty();
        assertThat(clients.<OAuth2AuthorizedClient>loadAuthorizedClient(UAA, OPERATOR_ID).block()).isNotNull();
    }

    @Test
    void aStoreTheTargetCannotActInIsRefusedAndTheTokenRevoked() {
        int revoked = REVOCATIONS.get();

        start(MERCHANT_ID, FOREIGN_STORE).expectStatus().isEqualTo(422)
                .expectBody().jsonPath(CODE).isEqualTo("GATEWAY.IMPERSONATION.STORE_NOT_TARGETS");

        assertThat(REVOCATIONS).hasValue(revoked + 1);
        assertThat(clients.loadAuthorizedClient(UAA, String.format(COMPOSITE, MERCHANT_ID, OPERATOR_ID)).blockOptional())
                .isEmpty();
    }

    /** Past the ceiling the next request is the operator's, before anything looks at it. */
    @Test
    void pastTheCeilingTheNextRequestIsTheOperatorAgain() {
        int revoked = REVOCATIONS.get();
        String session = sessionOf(start(EXPIRED, STORE).expectStatus().isOk().expectBody().returnResult());

        client.get().uri(IMPERSONATION).cookie(COOKIE, session).exchange().expectStatus().isNoContent();

        assertThat(REVOCATIONS).hasValue(revoked + 1);
        client.get().uri(ME).cookie(COOKIE, session).exchange().expectStatus().isOk()
                .expectBody().jsonPath(CLAIMS_SUB).isEqualTo(OPERATOR_ID);
        assertThat(clients.loadAuthorizedClient(UAA, String.format(COMPOSITE, EXPIRED, OPERATOR_ID)).blockOptional()).isEmpty();
    }

    private WebTestClient.ResponseSpec start(String userId, String storeId) {
        return client.mutateWith(mockAuthentication(operator())).post().uri(IMPERSONATION)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body(userId, storeId)).exchange();
    }

    private static Map<String, String> body(String userId, String storeId) {
        return Map.of("userId", userId, "storeId", storeId, "mode", READ, "reason", REASON);
    }

    private static OAuth2AuthenticationToken operator() {
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(SUPER_ADMIN)),
                Map.of(SUB, OPERATOR_ID, PREFERRED_USERNAME, OPERATOR), SUB);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), UAA);
    }

    private static String sessionOf(EntityExchangeResult<?> result) {
        ResponseCookie cookie = result.getResponseCookies().getFirst(COOKIE);
        assertThat(cookie).as("session cookie on %s", result.getResponseHeaders()).isNotNull();
        return cookie.getValue();
    }

    private static TestJwtSigner signer() {
        try {
            return new TestJwtSigner(new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString()).generate());
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static HttpServer stub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(TOKEN_PATH, ImpersonationFlowIntegrationTest::token);
            server.createContext("/oauth2/revoke", exchange -> {
                REVOCATIONS.incrementAndGet();
                answer(exchange, 200, JSON);
            });
            server.createContext("/api/v1/router/store-pod-by-store-id", exchange -> answer(exchange,
                    exchange.getRequestURI().getQuery().contains(String.format("store=%s", FOREIGN_STORE)) ? 403 : 200, JSON));
            server.start();
            return server;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** uaa's token endpoint, for the exchange only: the client authenticates, and the subject decides the answer. */
    private static void token(HttpExchange exchange) throws IOException {
        String authorization = exchange.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        Map<String, String> form = Arrays.stream(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
                        .split("&"))
                .map(pair -> pair.split("=", 2))
                .collect(Collectors.toMap(pair -> decode(pair[0]), pair -> pair.length > 1 ? decode(pair[1]) : ""));
        String subject = form.get("requested_subject");
        if (authorization == null || !authorization.startsWith("Basic ")
                || !"urn:ietf:params:oauth:grant-type:token-exchange".equals(form.get("grant_type")) || subject == null) {
            answer(exchange, 400, "{\"error\":\"invalid_request\"}");
            return;
        }
        if (REFUSED.equals(subject)) {
            answer(exchange, 400, "{\"error\":\"invalid_target\",\"error_description\":\"This account cannot be acted as.\"}");
            return;
        }
        // The back-dated token is still a well-formed one: issued before it expires, both in the past.
        Instant issuedAt = EXPIRED.equals(subject) ? Instant.now().minusSeconds(60) : Instant.now();
        Instant expiresAt = EXPIRED.equals(subject) ? Instant.now().minusSeconds(5) : Instant.now().plusSeconds(600);
        String mode = form.get("impersonation_mode");
        Map<String, Object> claims = new HashMap<>(Map.of(SUB, subject, UID, subject,
                "roles", List.of("STORE_ADMIN"), "store", form.get("impersonation_store"), PREFERRED_USERNAME, MERCHANT,
                "act", Map.of(SUB, OPERATOR, UID, OPERATOR_ID), "act_mode", mode));
        claims.put("iat", issuedAt.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());
        String jwt = SIGNER.sign(claims);
        answer(exchange, 200, String.format("{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":600,"
                + "\"act_mode\":\"%s\",\"acting_as\":\"%s\"}", jwt, mode, MERCHANT));
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void answer(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

}
