package com.asrevo.cvhome.gateway.config;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.podregistry.api.ReactiveExternalPodService;
import com.asrevo.cvhome.testsupport.annotations.ReactiveIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockAuthentication;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

/**
 * The gateway's actuator over the real filter chain: health is the probe and stays open; everything else under
 * {@code /actuator} is a super admin's read. Asserted on {@code /actuator/info} rather than {@code /env} for the
 * signed-in cases, because {@code info} is mapped whatever {@code common-config.yml} exposes.
 */
@ReactiveIntegrationTest
class ActuatorGateIntegrationTest {

    private static final String INFO = "/actuator/info";

    private static final String SUB = "sub";

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private ReactiveExternalPodService podService;

    @MockitoBean
    private ReactiveExternalEntitlementService entitlementService;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToApplicationContext(context).apply(springSecurity()).configureClient().build();
    }

    private static OAuth2AuthenticationToken session(String role) {
        DefaultOAuth2User user = new DefaultOAuth2User(List.of(new SimpleGrantedAuthority(role)),
                Map.of(SUB, "operator"), SUB);
        return new OAuth2AuthenticationToken(user, user.getAuthorities(), "uaa");
    }

    /**
     * The probe reaches health without a session. Its aggregate is whatever the mocked dependencies report — DOWN
     * here — so the assertion is that security let the request through and the body carries a status, not that the
     * gateway is healthy in a test context.
     */
    @Test
    void healthIsOpenToTheProbe() {
        client.get().uri("/actuator/health").exchange()
                .expectStatus().value(status -> assertThat(status).isNotIn(401, 403))
                .expectBody().jsonPath("$.status").exists();
    }

    @Test
    void theRestOfTheActuatorIsUnauthorizedForAnAnonymousCallerAndNoRedirect() {
        for (String endpoint : new String[] {INFO, "/actuator/env", "/actuator/heapdump", "/actuator/gateway/routes"}) {
            client.get().uri(endpoint).exchange().expectStatus().isUnauthorized();
        }
    }

    @Test
    void aSuperAdminSessionReadsIt() {
        client.mutateWith(mockAuthentication(session(SecurityConfig.SUPER_ADMIN)))
                .get().uri(INFO).exchange().expectStatus().isOk();
    }

    @Test
    void anOrgAdminSessionIsForbidden() {
        client.mutateWith(mockAuthentication(session("ROLE_ORG_ADMIN")))
                .get().uri(INFO).exchange().expectStatus().isForbidden();
    }

}
