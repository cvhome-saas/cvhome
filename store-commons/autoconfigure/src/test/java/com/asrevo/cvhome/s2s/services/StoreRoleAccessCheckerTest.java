package com.asrevo.cvhome.s2s.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.jwt.RealmAwareJwtGrantedAuthoritiesConverter;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The predicates every store-scoped authorization decision is assembled from.
 *
 * <p>
 * Each one answers a single question — is this principal that role, for <em>this</em> store, on <em>this</em> pod —
 * and the negative answers are what matter. A staff token is scoped to exactly one store, so the check that the
 * requested store matches the token's own is the only thing preventing one merchant's admin from reading another
 * merchant's shop through the same endpoint.
 * </p>
 */
class StoreRoleAccessCheckerTest {

    private static final String ORG_CLAIM = "org";

    private static final String STORE_CLAIM = "store";

    private static final String RESOURCE_CLAIM = "resource";

    /** The user pool a shopper token was minted against — the store. */
    private static final String REALM_CLAIM = SecurityUtils.USER_REALM_CLAIM;

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String OTHER_ORG = "32a034a43cd77581d105c87a";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final String POD_NAME = "pod-1";

    private static final String CUA = "cua";

    private static final String UAA = "uaa";

    /** STORE belongs to ORG; every other store this file names belongs to nobody it asks about. */
    private final StoreRoleAccessChecker checker =
            new StoreRoleAccessChecker(() -> store -> STORE.equals(store) ? new ManagerOrgId(ORG) : null,
                    StoreOwnershipPolicy.ENFORCED);

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

    /** As {@link #principal}, plus the {@code REALM_} authority the authorities converter stamps on. */
    private static Authentication fromRealm(String realm, Map<String, Object> claims, Roles... roles) {
        Authentication base = principal(claims, roles);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        base.getAuthorities().forEach(it -> authorities.add(new SimpleGrantedAuthority(it.getAuthority())));
        authorities.add(new SimpleGrantedAuthority(
                RealmAwareJwtGrantedAuthoritiesConverter.REALM_AUTHORITY_PREFIX + realm));
        return new JwtAuthenticationToken((Jwt) base.getPrincipal(), authorities);
    }

    private static Authentication staff(Roles role, String org, StoreMerchantId store) {
        return principal(Map.of(ORG_CLAIM, org, STORE_CLAIM, store.storeMerchantId()), role);
    }

    private static Pod pod(String org) {
        return new Pod(null, POD_NAME, null, org == null ? null : new ManagerOrgId(org), null);
    }

    @Nested
    class OrgAdmins {

        @Test
        void anOrgAdminIsRecognisedAsOne() {
            assertThat(checker.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), STORE)).isTrue();
        }

        @Test
        void aStoreAdminIsNotAnOrgAdmin() {
            assertThat(checker.isOrgAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE)).isFalse();
        }

        @Test
        void anOrgAdminIsAdmittedOnAPodDedicatedToItsOwnOrganization() {
            assertThat(checker.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), STORE, pod(ORG)))
                    .isTrue();
        }

        @Test
        void anOrgAdminIsRefusedOnAPodDedicatedToAnotherOrganization() {
            assertThat(checker.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), STORE,
                    pod(OTHER_ORG))).isFalse();
        }

        @Test
        void aSharedPodConstrainsNobody() {
            assertThat(checker.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), STORE, pod(null)))
                    .isTrue();
        }

        /**
         * The store is the one thing the token cannot vouch for.
         *
         * <p>
         * An org admin's token names the organization they administer and says nothing about who owns the store
         * in the query parameter — and on a shared pod every other tenant's store is one query parameter away.
         * This used to pass, under a {@code @TODO} saying it should not: an org admin of one organization could
         * read another's store, and the tenancy was never wrong about it — the realm switched correctly and
         * returned exactly that realm's rows, to somebody who should not have been asking.
         * </p>
         */
        @Test
        void anOrgAdminIsRefusedOnAStoreItsOrganizationDoesNotOwn() {
            assertThat(checker.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), OTHER_STORE))
                    .isFalse();
        }

        /**
         * A service that checks for itself keeps the gate out of it.
         *
         * <p>
         * Tenancy is the one, and it earns it: it answers a foreign store with "not found" rather than
         * "forbidden", so asking about somebody else's store does not confirm the store exists — which a gate
         * that can only say yes or no cannot express. Declaring this is declaring that the check happens
         * elsewhere.
         * </p>
         */
        @Test
        void aServiceThatChecksForItselfIsLeftToIt() {
            StoreRoleAccessChecker delegating = new StoreRoleAccessChecker(() -> null, StoreOwnershipPolicy.DELEGATED);

            assertThat(delegating.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), OTHER_STORE))
                    .isTrue();
        }

        /** A check that cannot be made has not passed, so a service with no lookup admits no org admin. */
        @Test
        void anOrgAdminIsRefusedWhereTheOwnerCannotBeEstablished() {
            StoreRoleAccessChecker blind = new StoreRoleAccessChecker(() -> null, StoreOwnershipPolicy.ENFORCED);

            assertThat(blind.isOrgAdmin(principal(Map.of(ORG_CLAIM, ORG), Roles.ROLE_ORG_ADMIN), STORE)).isFalse();
        }
    }

    @Nested
    class StoreStaff {

        @Test
        void aStoreAdminMayActOnItsOwnStore() {
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE)).isTrue();
        }

        /** The isolation boundary: one merchant's admin reaching another merchant's shop. */
        @Test
        void aStoreAdminMayNotActOnAnotherStore() {
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), OTHER_STORE)).isFalse();
        }

        @Test
        void aModeratorMayActOnItsOwnStoreAndNoOther() {
            Authentication moderator = staff(Roles.ROLE_STORE_MODERATOR, ORG, STORE);

            assertThat(checker.isStoreModerator(moderator, STORE)).isTrue();
            assertThat(checker.isStoreModerator(moderator, OTHER_STORE)).isFalse();
        }

        @Test
        void aStoreAdminIsNotAModeratorAndAModeratorIsNotAnAdmin() {
            assertThat(checker.isStoreModerator(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE)).isFalse();
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_MODERATOR, ORG, STORE), STORE)).isFalse();
        }

        @Test
        void staffAreRefusedOnAPodDedicatedToAnotherOrganization() {
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE, pod(OTHER_ORG)))
                    .isFalse();
            assertThat(checker.isStoreModerator(staff(Roles.ROLE_STORE_MODERATOR, ORG, STORE), STORE,
                    pod(OTHER_ORG))).isFalse();
        }

        @Test
        void staffAreAdmittedOnTheirOwnOrganizationsPod() {
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE, pod(ORG))).isTrue();
            assertThat(checker.isStoreModerator(staff(Roles.ROLE_STORE_MODERATOR, ORG, STORE), STORE, pod(ORG)))
                    .isTrue();
        }
    }

    @Nested
    class Customers {

        /**
         * A shopper's token identifies its store through the {@code clientId} attribute rather than the
         * {@code store} claim staff tokens carry — the storefront's OAuth client <em>is</em> the store.
         */
        @Test
        void aCustomerMayActOnTheStoreItsClientBelongsTo() {
            Authentication customer = principal(Map.of(REALM_CLAIM, STORE.getId()), Roles.ROLE_CUSTOMER);

            assertThat(checker.isStoreCustomer(customer, STORE)).isTrue();
        }

        @Test
        void aCustomerMayNotActOnAnotherStore() {
            Authentication customer = principal(Map.of(REALM_CLAIM, STORE.getId()), Roles.ROLE_CUSTOMER);

            assertThat(checker.isStoreCustomer(customer, OTHER_STORE)).isFalse();
        }

        @Test
        void aCustomerTokenWithoutAClientBelongsToNoStore() {
            assertThat(checker.isStoreCustomer(principal(Map.of(), Roles.ROLE_CUSTOMER), STORE)).isFalse();
        }

        @Test
        void staffAreNotCustomers() {
            assertThat(checker.isStoreCustomer(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE)).isFalse();
        }
    }

    @Nested
    class ServicePrincipals {

        @Test
        void aPlatformOperatorIsRecognised() {
            assertThat(checker.isSuperAdmin(principal(Map.of(), Roles.ROLE_SUPER_ADMIN))).isTrue();
            assertThat(checker.isSuperAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE))).isFalse();
        }

        @Test
        void theCoreScopeIsRecognised() {
            assertThat(checker.isScopeStoreCore(principal(Map.of(), Roles.SCOPE_STORE_CORE))).isTrue();
            assertThat(checker.isScopeStoreCore(principal(Map.of(), Roles.SCOPE_STORE_POD))).isFalse();
        }

        /**
         * A pod-scoped service token names the pod it was minted for. Admitting one whose {@code resource} names a
         * different pod would let a service on one pod act on another's data.
         */
        @Test
        void aPodScopedPrincipalIsAdmittedOnlyOnThePodItsTokenNames() {
            Authentication onPod = principal(Map.of(RESOURCE_CLAIM, POD_NAME), Roles.SCOPE_STORE_POD);

            assertThat(checker.isScopeStorePod(onPod, pod(null))).isTrue();
            assertThat(checker.isScopeStorePod(onPod, new Pod(null, "pod-2", null, null, null))).isFalse();
        }

        @Test
        void withoutAPodThereIsNothingToMatchAndTheAnswerIsNo() {
            assertThat(checker.isScopeStorePod(principal(Map.of(RESOURCE_CLAIM, POD_NAME), Roles.SCOPE_STORE_POD),
                    null)).isFalse();
        }

        @Test
        void aPrincipalWithoutThePodScopeIsRefused() {
            assertThat(checker.isScopeStorePod(principal(Map.of(RESOURCE_CLAIM, POD_NAME), Roles.SCOPE_STORE_CORE),
                    pod(null))).isFalse();
        }
    }

    /**
     * The second guard on the boundary between the two authorization servers.
     *
     * <p>
     * The realm cap in the authorities converter is the primary one: a cua token cannot carry a staff role out of
     * the converter at all. These tests cover the case where it somehow does — a misconfigured realm, a legacy
     * flat trust list widened by mistake — and prove the check itself still refuses to read a shopper principal
     * as staff, or a staff principal as a shopper.
     * </p>
     */
    @Nested
    class Realms {

        @Test
        void aShopperPrincipalIsNotReadAsStaffEvenIfItClaimsAStaffRole() {
            Authentication forged = fromRealm(CUA, Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()),
                    Roles.ROLE_STORE_ADMIN);

            assertThat(checker.isStoreAdmin(forged, STORE, pod(null))).isFalse();
            assertThat(checker.isSuperAdmin(forged)).isFalse();
            assertThat(checker.isScopeStoreCore(forged)).isFalse();
        }

        @Test
        void aStaffPrincipalIsNotReadAsAShopper() {
            Authentication staffToken = fromRealm(UAA, Map.of(REALM_CLAIM, STORE.getId()), Roles.ROLE_CUSTOMER);

            assertThat(checker.isStoreCustomer(staffToken, STORE)).isFalse();
        }

        @Test
        void aPrincipalFromTheRightRealmIsUnaffected() {
            Authentication shopper = fromRealm(CUA, Map.of(REALM_CLAIM, STORE.getId()), Roles.ROLE_CUSTOMER);
            Authentication admin = fromRealm(UAA, Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()),
                    Roles.ROLE_STORE_ADMIN);

            assertThat(checker.isStoreCustomer(shopper, STORE)).isTrue();
            assertThat(checker.isStoreAdmin(admin, STORE, pod(null))).isTrue();
        }

        /** No realm authority means realms are not configured, and the checks must behave exactly as before. */
        @Test
        void aPrincipalWithNoRealmIsJudgedOnItsRolesAlone() {
            assertThat(checker.isStoreAdmin(staff(Roles.ROLE_STORE_ADMIN, ORG, STORE), STORE, pod(null))).isTrue();
            assertThat(checker.isStoreCustomer(principal(Map.of(REALM_CLAIM, STORE.getId()),
                    Roles.ROLE_CUSTOMER), STORE)).isTrue();
        }
    }
}
