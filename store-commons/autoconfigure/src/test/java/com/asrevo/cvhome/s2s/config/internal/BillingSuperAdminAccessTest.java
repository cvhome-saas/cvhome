package com.asrevo.cvhome.s2s.config.internal;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The whole authorization decision behind the platform's billing screens, in one file.
 *
 * <p>
 * A platform operator could read no store's subscription and no store's invoices on this platform, because
 * {@code PermissionAccessChecker.hasAccessOnBillingRead} resolved to {@code hasReadAccessOnStore} — which admits an
 * org admin, a store admin, a store moderator and a store-core service principal, and has no super-admin branch. So
 * "this merchant says they paid", the support question a platform console exists to answer, could only be answered
 * by reading the database.
 * </p>
 *
 * <p>
 * <strong>The branch is on the two billing tokens and on nothing else, and that is what these tests hold.</strong>
 * Widening the shared {@code hasReadAccessOnStore} instead would have handed a platform operator every store-scoped
 * screen in the console — the merchant pages the shell's {@code platformOnly} guard deliberately hides from them,
 * on the assumption that they 403. The last two tests are what keeps that assumption true.
 * </p>
 */
@Tag("unit-test")
class BillingSuperAdminAccessTest {

    private static final String TARGET_TYPE = "StoreMerchantId";

    private static final String ORG_CLAIM = "org";

    private static final String STORE_CLAIM = "store";

    private static final String BILLING_READ = "STORE-CORE.BILLING.READ";

    private static final String BILLING_MANAGE = "STORE-CORE.BILLING.MANAGE";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator(new StaticApplicationContext());

    @ParameterizedTest(name = "a super admin may {0} on any store")
    @DisplayName("a super admin holds both billing tokens for a store it does not own")
    @ValueSource(strings = {BILLING_READ, BILLING_MANAGE})
    void superAdminHoldsBothBillingTokens(String token) {
        // No `store` claim and no org: a platform operator owns nothing, which is exactly why the store-scoped
        // branches all refused them.
        assertThat(evaluator.hasPermission(superAdmin(), STORE, TARGET_TYPE, token)).isTrue();
    }

    @Test
    @DisplayName("a super admin still cannot read a store")
    void superAdminDoesNotGainStoreReads() {
        // The merchant console's own screens. `platformOnly` redirects an operator away from them; this is what
        // makes that a UI convenience rather than the only thing standing between them and another org's shop.
        assertThat(evaluator.hasPermission(superAdmin(), STORE, TARGET_TYPE, "STORE-CORE.STORE-FIND-ONE")).isFalse();
    }

    @Test
    @DisplayName("a super admin still cannot administer a store's users")
    void superAdminDoesNotGainUserAdministration() {
        assertThat(evaluator.hasPermission(superAdmin(), STORE, TARGET_TYPE, "STORE-CORE.USERS.RESET_PASSWORD"))
                .isFalse();
    }

    @Test
    @DisplayName("an org admin keeps both billing tokens — the widening took nothing away")
    void orgAdminIsUnchanged() {
        // The regression the change actually risks: the super-admin branch is an `||`, so the existing audience has
        // to be untouched. A merchant losing their own billing page is the failure this catches.
        assertThat(evaluator.hasPermission(orgAdmin(), STORE, TARGET_TYPE, BILLING_READ)).isTrue();
        assertThat(evaluator.hasPermission(orgAdmin(), STORE, TARGET_TYPE, BILLING_MANAGE)).isTrue();
    }

    @Test
    @DisplayName("a store moderator may still read billing and may not change it")
    void moderatorReadsButDoesNotManage() {
        // Unchanged by this edit, and asserted anyway: `manage` is the endpoint that leads to a charge.
        assertThat(evaluator.hasPermission(moderator(), STORE, TARGET_TYPE, BILLING_READ)).isTrue();
        assertThat(evaluator.hasPermission(moderator(), STORE, TARGET_TYPE, BILLING_MANAGE)).isFalse();
    }

    @Test
    @DisplayName("an ordinary customer holds neither billing token")
    void aCustomerHoldsNeither() {
        Authentication customer = principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_CUSTOMER);

        assertThat(evaluator.hasPermission(customer, STORE, TARGET_TYPE, BILLING_READ)).isFalse();
        assertThat(evaluator.hasPermission(customer, STORE, TARGET_TYPE, BILLING_MANAGE)).isFalse();
    }

    private static Authentication superAdmin() {
        return principal(Map.of(), Roles.ROLE_SUPER_ADMIN);
    }

    private static Authentication orgAdmin() {
        return principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN);
    }

    private static Authentication moderator() {
        return principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()),
                Roles.ROLE_STORE_MODERATOR);
    }

    /**
     * A real {@link JwtAuthenticationToken} rather than a mock: the code under test reads both the authorities and
     * the token's own claims, so a stub answering only one of them would pass while the production path failed.
     */
    private static Authentication principal(Map<String, Object> claims, Roles... roles) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("a-principal")
                .issuedAt(Instant.EPOCH)
                .expiresAt(Instant.EPOCH.plusSeconds(3600))
                .claims(existing -> existing.putAll(claims))
                .claim("roles", Set.of(roles))
                .build();
        return new JwtAuthenticationToken(jwt,
                List.of(roles).stream().map(role -> new SimpleGrantedAuthority(role.name())).toList());
    }

}
