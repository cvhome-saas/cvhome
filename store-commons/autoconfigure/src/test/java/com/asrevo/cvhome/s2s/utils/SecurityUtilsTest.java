package com.asrevo.cvhome.s2s.utils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Who a request is, reduced to the org and store it may touch. Every store-scoped query in the platform is filtered
 * by what this returns, so a wrong answer here is a cross-tenant data leak rather than a failed request.
 *
 * <p>
 * The ordering of the ladder is the load-bearing detail and the reason this file exists. <strong>The scope is tested
 * before the role</strong>, so an org administrator whose token carries {@code store_core} resolves to
 * platform-wide access with a <em>null</em> org — the {@code org} claim is never read. Any isolation test written
 * against such a token passes without proving anything, which is exactly what happened to this codebase once.
 * </p>
 */
class SecurityUtilsTest {

    private static final String ORG_CLAIM = "org";

    private static final String STORE_CLAIM = "store";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String WILDCARD = "*";

    private static final String TOKEN_VALUE = "token";

    private static final String ALG = "alg";

    private static final String NONE = "none";

    private static final String SUBJECT = "a-principal";

    private static Authentication principal(Map<String, Object> claims, Roles... roles) {
        Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE)
                .header(ALG, NONE)
                .subject(SUBJECT)
                .issuedAt(Instant.EPOCH)
                .expiresAt(Instant.EPOCH.plusSeconds(3600))
                .claims(existing -> existing.putAll(claims))
                .claim("roles", Set.of(roles))
                .build();
        return new JwtAuthenticationToken(jwt,
                List.of(roles).stream().map(role -> new SimpleGrantedAuthority(role.name())).toList());
    }

    @Test
    void aPlatformOperatorSeesEveryStoreAndBelongsToNoOrg() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(principal(Map.of(),
                Roles.ROLE_SUPER_ADMIN));

        assertThat(identity.org()).isNull();
        assertThat(identity.store().storeMerchantId()).isEqualTo(WILDCARD);
    }

    @Test
    void aServicePrincipalOnTheCoreScopeSeesEveryStoreAndBelongsToNoOrg() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(principal(Map.of(),
                Roles.SCOPE_STORE_CORE));

        assertThat(identity.org()).isNull();
        assertThat(identity.store().storeMerchantId()).isEqualTo(WILDCARD);
    }

    @Test
    void anOrgAdminSeesEveryStoreItsOrganizationOwnsAndNothingElse() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(
                principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN));

        assertThat(identity.org().id()).hasToString(ORG);
        assertThat(identity.store().storeMerchantId()).isEqualTo(WILDCARD);
    }

    /**
     * The trap. An org admin minted on {@code store_core} never reaches the org-admin branch, so its {@code org}
     * claim is discarded and it resolves as a platform operator. A cross-organization isolation test written against
     * such a token asserts nothing at all.
     */
    @Test
    void anOrgAdminCarryingTheCoreScopeResolvesAsPlatformWideWithNoOrgAtAll() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(
                principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN, Roles.SCOPE_STORE_CORE));

        assertThat(identity.org()).isNull();
        assertThat(identity.store().storeMerchantId()).isEqualTo(WILDCARD);
    }

    @Test
    void aStoreScopedPrincipalSeesOnlyItsOwnStore() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(
                principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE), Roles.ROLE_STORE_ADMIN));

        assertThat(identity.org().id()).hasToString(ORG);
        assertThat(identity.store().storeMerchantId()).isEqualTo(STORE);
    }

    @Test
    void aStoreScopedPrincipalWithoutAStoreClaimGetsNoStoreRatherThanTheWildcard() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(
                principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_STORE_ADMIN));

        assertThat(identity.org().id()).hasToString(ORG);
        assertThat(identity.store()).isNull();
    }

    @Test
    void theIdentityCarriesTheRolesTheTokenGranted() {
        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(
                principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE), Roles.ROLE_STORE_ADMIN,
                        Roles.SCOPE_STORE_POD));

        assertThat(identity.roles()).containsExactlyInAnyOrder(Roles.ROLE_STORE_ADMIN, Roles.SCOPE_STORE_POD);
    }

    @Test
    void anAuthorityThatIsNotOneOfOurRolesIsDroppedRatherThanFailingTheRequest() {
        Authentication authentication = new JwtAuthenticationToken(
                Jwt.withTokenValue(TOKEN_VALUE).header(ALG, NONE).subject(SUBJECT)
                        .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600))
                        .claim(ORG_CLAIM, ORG).claim(STORE_CLAIM, STORE).build(),
                List.of(new SimpleGrantedAuthority("SOMETHING_ELSE"),
                        new SimpleGrantedAuthority(Roles.ROLE_STORE_ADMIN.name())));

        UserOrgStoreIdentity identity = SecurityUtils.getOrgStoreIdentity(authentication);

        assertThat(identity.roles()).containsExactly(Roles.ROLE_STORE_ADMIN);
    }

    @Test
    void theRoleAndScopePredicatesReadTheGrantedAuthorities() {
        Authentication storeAdmin = principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE), Roles.ROLE_STORE_ADMIN,
                Roles.SCOPE_STORE_POD);

        assertThat(SecurityUtils.hasRole(storeAdmin, Roles.ROLE_STORE_ADMIN)).isTrue();
        assertThat(SecurityUtils.hasRole(storeAdmin, Roles.ROLE_SUPER_ADMIN)).isFalse();
        assertThat(SecurityUtils.hasScopeStorePod(storeAdmin)).isTrue();
        assertThat(SecurityUtils.getRoles(storeAdmin))
                .containsExactlyInAnyOrder(Roles.ROLE_STORE_ADMIN.name(), Roles.SCOPE_STORE_POD.name());
    }
}
