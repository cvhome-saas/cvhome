package com.asrevo.cvhome.sso.realm;

import java.time.Instant;
import java.util.Map;

import org.hibernate.cfg.AvailableSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;

import com.asrevo.cvhome.commons.domain.RealmId;
import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * The realm wiring: the seam's default filler, Hibernate's tenant resolver, and the two filters' order.
 *
 * <p>
 * <strong>A {@code MULTI} deployment with no resolver is refused at startup.</strong> Falling back to the fixed
 * resolver there would serve every store from one realm — one user pool shared by every tenant — and it would do
 * so silently, which is the worst possible way for a multi-tenant system to be misconfigured.
 * </p>
 *
 * <p>
 * The filter order is not decoration. The realm filter runs at highest precedence because authenticating a
 * username is only meaningful once the realm is known; the session realm filter runs after Spring Session's
 * filter (at {@code MIN_VALUE + 50}) and before the security chain, so the session is readable but authentication
 * has not read it yet.
 * </p>
 */
class RealmConfigTest {

    private static final String FIXED = "platform";
    private static final String STORE_1 = "store-1";
    private static final String STORE_ONE = "Store One";

    private final RealmConfig config = new RealmConfig();

    @Test
    void asingleRealmDeploymentGetsTheFixedResolverBySayingNothing() {
        RealmResolver resolver = config.realmResolver(properties(RealmMode.SINGLE));

        assertThat(resolver.resolve(new MockHttpServletRequest())).isEqualTo(RealmId.of(FIXED));
    }

    @Test
    void amultiRealmDeploymentWithNoResolverIsRefusedAtStartupRatherThanSharingOneUserPool() {
        SsoRealmProperties properties = properties(RealmMode.MULTI);

        assertThatThrownBy(() -> config.realmResolver(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RealmResolver");
    }

    @Test
    void hibernateIsGivenTheTenantResolverExplicitlyRatherThanByDetection() {
        SsoTenantIdentifierResolver resolver = config.ssoTenantIdentifierResolver(properties(RealmMode.SINGLE));
        Map<String, Object> hibernate = new java.util.HashMap<>();

        config.realmTenantIdentifier(resolver).customize(hibernate);

        // Registered by hand so a refactor cannot quietly turn realm filtering off.
        assertThat(hibernate).containsEntry(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
    }

    @Test
    void therealmFilterRunsAheadOfEverythingElse() {
        FilterRegistrationBean<RealmFilter> registration = config.realmFilter(
                request -> RealmId.of(FIXED), mock(ProblemDetailFactory.class), JsonMapper.builder().build());

        assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
        assertThat(registration.getFilter()).isInstanceOf(RealmFilter.class);
    }

    @Test
    void theSessionRealmFilterRunsAfterSpringSessionAndBeforeTheSecurityChain() {
        FilterRegistrationBean<SessionRealmFilter> registration =
                config.sessionRealmFilter(mock(ProblemDetailFactory.class), JsonMapper.builder().build());

        // Spring Session's own filter sits at MIN_VALUE + 50; this has to read the session it restores.
        assertThat(registration.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE + 60);
        assertThat(registration.getFilter()).isInstanceOf(SessionRealmFilter.class);
    }

    @Test
    void arealmRowCarriesItsIdNameAndAcreationStamp() {
        Realm realm = new Realm(STORE_1, STORE_ONE);

        assertThat(realm.getId()).isEqualTo(STORE_1);
        assertThat(realm.getDisplayName()).isEqualTo(STORE_ONE);
        // Enabled by default: a row exists precisely to say the realm is servable.
        assertThat(realm.isEnabled()).isTrue();
        assertThat(realm.getCreatedAt()).isNotNull().isBefore(Instant.now().plusSeconds(1));
        Realm same = new Realm(STORE_1, STORE_ONE);
        same.setCreatedAt(realm.getCreatedAt());
        assertThat(realm).isEqualTo(same).hasSameHashCodeAs(same);
        assertThat(realm.toString()).contains(STORE_1);
    }

    @Test
    void adisabledRealmIsNoLongerServable() {
        Realm realm = new Realm(STORE_1, STORE_ONE);
        Realm enabled = new Realm(STORE_1, STORE_ONE);
        enabled.setCreatedAt(realm.getCreatedAt());
        realm.setEnabled(false);

        assertThat(realm.isEnabled()).isFalse();
        assertThat(realm).isNotEqualTo(enabled);
    }

    private static SsoRealmProperties properties(RealmMode mode) {
        SsoRealmProperties properties = new SsoRealmProperties();
        properties.setMode(mode);
        properties.setFixed(FIXED);
        return properties;
    }

}
