package com.asrevo.cvhome.s2s.services;

import java.time.Instant;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The audience of every {@code @PreAuthorize} in the platform, one method per endpoint family.
 *
 * <p>
 * {@link StoreRoleAccessChecker} answers "is this principal that role"; this class decides which of those answers
 * each operation accepts, and the interesting part is where two families that look alike differ. Reading a store
 * admits a moderator; maintaining its users does not. Reading billing admits the platform operator; reading the
 * store itself does not. Reading a store's entitlements admits a pod service principal, because the pods are what
 * enforce the ceilings, while reading its invoices does not.
 * </p>
 *
 * <p>
 * Those asymmetries are deliberate and each one is a past incident: the javadoc on
 * {@link PermissionAccessChecker#hasAccessOnBillingRead} and
 * {@link PermissionAccessChecker#hasAccessOnBillingEntitlementRead} records a super admin being refused every
 * store's billing, and every pod being refused the ceilings it enforces. Widening or narrowing one family by
 * copying another is exactly how they come back.
 * </p>
 */
class PermissionAccessCheckerTest {

    private static final String ORG_CLAIM = "org";
    private static final String STORE_CLAIM = "store";
    private static final String SCOPE_CLAIM = "scope";
    private static final String ORG = "21f023932bc66470c104b76f";
    private static final String OTHER_ORG = "32a034a43cd77581d105c87a";
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final StoreMerchantId OTHER_STORE = new StoreMerchantId("65f023632bc46470c104b75f");
    private static final String POD_NAME = "pod-1";

    /** STORE belongs to ORG; nothing else this file names belongs to anybody. */
    private final PermissionAccessChecker checker =
            new PermissionAccessChecker(() -> store -> STORE.equals(store) ? new ManagerOrgId(ORG) : null,
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

    private static Authentication staff(Roles role) {
        return principal(Map.of(ORG_CLAIM, ORG, STORE_CLAIM, STORE.storeMerchantId()), role);
    }

    private static Authentication orgAdmin(String org) {
        return principal(Map.of(ORG_CLAIM, org), Roles.ROLE_ORG_ADMIN);
    }

    private static Authentication superAdmin() {
        return principal(Map.of(), Roles.ROLE_SUPER_ADMIN);
    }

    private static Authentication service(Roles scope) {
        return principal(Map.of(SCOPE_CLAIM, scope.name()), scope);
    }

    @Nested
    class ReadingAStore {

        @Test
        void anOrgAdminAStoreAdminAndAModeratorMayAllRead() {
            assertThat(checker.hasAccessOnStoreFindOne(orgAdmin(ORG), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreFindOne(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreFindOne(staff(Roles.ROLE_STORE_MODERATOR), STORE)).isTrue();
        }

        @Test
        void aStoreCoreServicePrincipalMayRead() {
            assertThat(checker.hasAccessOnStoreFindOne(service(Roles.SCOPE_STORE_CORE), STORE)).isTrue();
        }

        @Test
        void anotherOrganizationsAdminMayNot() {
            assertThat(checker.hasAccessOnStoreFindOne(orgAdmin(OTHER_ORG), STORE)).isFalse();
        }

        @Test
        void aCustomerMayNot() {
            assertThat(checker.hasAccessOnStoreFindOne(staff(Roles.ROLE_CUSTOMER), STORE)).isFalse();
        }

        @Test
        void aStoreAdminMayNotReadAnotherStore() {
            assertThat(checker.hasAccessOnStoreFindOne(staff(Roles.ROLE_STORE_ADMIN), OTHER_STORE)).isFalse();
        }
    }

    @Nested
    class AdministeringStoreUsers {

        @Test
        void anOrgAdminAndAStoreAdminMayMaintainUsers() {
            assertThat(checker.hasAccessOnStoreUsersCreate(orgAdmin(ORG), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreUsersUpdate(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreUsersDelete(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreUsersEnable(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreUsersDisable(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
        }

        @Test
        void aModeratorMayListUsersButNotMaintainThem() {
            Authentication moderator = staff(Roles.ROLE_STORE_MODERATOR);
            assertThat(checker.hasAccessOnStoreUsersList(moderator, STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreUsersCreate(moderator, STORE)).isFalse();
            assertThat(checker.hasAccessOnStoreUsersDelete(moderator, STORE)).isFalse();
        }

        @Test
        void resettingAnotherUsersPasswordIsMaintenanceNotReading() {
            // A moderator can see who has access to a store without being able to take it over.
            assertThat(checker.hasAccessOnStoreUsersResetPassword(staff(Roles.ROLE_STORE_MODERATOR), STORE)).isFalse();
            assertThat(checker.hasAccessOnStoreUsersResetPassword(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
        }
    }

    @Nested
    class CreatingDeletingAndManagingAStore {

        @Test
        void onlyAStoreCoreServicePrincipalMayCreateAStore() {
            assertThat(checker.hasAccessOnStoreCreate(service(Roles.SCOPE_STORE_CORE), ORG, null)).isTrue();
            assertThat(checker.hasAccessOnStoreCreate(orgAdmin(ORG), ORG, null)).isFalse();
        }

        @Test
        void aStoreMayNotBeCreatedOnAPodDedicatedToAnotherOrganization() {
            Pod dedicated = new Pod(null, POD_NAME, null, new ManagerOrgId(OTHER_ORG), null);
            assertThat(checker.hasAccessOnStoreCreate(service(Roles.SCOPE_STORE_CORE), ORG, dedicated)).isFalse();
        }

        @Test
        void aStoreMayBeCreatedOnItsOwnOrganizationsPodAndOnASharedOne() {
            Pod dedicated = new Pod(null, POD_NAME, null, new ManagerOrgId(ORG), null);
            Pod shared = new Pod(null, POD_NAME, null, null, null);
            assertThat(checker.hasAccessOnStoreCreate(service(Roles.SCOPE_STORE_CORE), ORG, dedicated)).isTrue();
            assertThat(checker.hasAccessOnStoreCreate(service(Roles.SCOPE_STORE_CORE), ORG, shared)).isTrue();
        }

        @Test
        void deletingAStoreIsTheOrgAdminsAloneNotTheStoreAdmins() {
            assertThat(checker.hasAccessOnStoreDelete(orgAdmin(ORG), STORE)).isTrue();
            assertThat(checker.hasAccessOnStoreDelete(staff(Roles.ROLE_STORE_ADMIN), STORE)).isFalse();
            assertThat(checker.hasAccessOnStoreDelete(staff(Roles.ROLE_STORE_MODERATOR), STORE)).isFalse();
        }

        @Test
        void managingAStoreAdmitsItsOwnAdminAndItsOrgAdmin() {
            assertThat(checker.hasManageAccessOnStore(orgAdmin(ORG), STORE, null)).isTrue();
            assertThat(checker.hasManageAccessOnStore(staff(Roles.ROLE_STORE_ADMIN), STORE, null)).isTrue();
            assertThat(checker.hasManageAccessOnStore(staff(Roles.ROLE_STORE_MODERATOR), STORE, null)).isFalse();
        }
    }

    @Nested
    class Billing {

        @Test
        void thePlatformOperatorReadsAndManagesEveryStoresBilling() {
            // The incident this branch exists for: billing refused a super admin for every store on the platform.
            assertThat(checker.hasAccessOnBillingRead(superAdmin(), STORE)).isTrue();
            assertThat(checker.hasAccessOnBillingManage(superAdmin(), STORE)).isTrue();
        }

        @Test
        void thatWideningIsBillingsAloneAndDoesNotLeakIntoTheMerchantScreens() {
            assertThat(checker.hasAccessOnStoreFindOne(superAdmin(), STORE)).isFalse();
            assertThat(checker.hasAccessOnStoreUsersList(superAdmin(), STORE)).isFalse();
        }

        @Test
        void aModeratorSeesThePlanItWorksUnderButMayNotChangeWhatItCosts() {
            assertThat(checker.hasAccessOnBillingRead(staff(Roles.ROLE_STORE_MODERATOR), STORE)).isTrue();
            assertThat(checker.hasAccessOnBillingManage(staff(Roles.ROLE_STORE_MODERATOR), STORE)).isFalse();
        }

        @Test
        void spendingMoneyIsAnOrgLevelActSoAStoreAdminMayNot() {
            assertThat(checker.hasAccessOnBillingManage(orgAdmin(ORG), STORE)).isTrue();
            assertThat(checker.hasAccessOnBillingManage(staff(Roles.ROLE_STORE_ADMIN), STORE)).isFalse();
        }

        @Test
        void aPodServicePrincipalReadsEntitlementsBecauseThePodsEnforceThem() {
            // The other incident: routing this through isScopeStorePod denied every pod that asked.
            assertThat(checker.hasAccessOnBillingEntitlementRead(service(Roles.SCOPE_STORE_POD), STORE)).isTrue();
            assertThat(checker.hasAccessOnBillingEntitlementRead(service(Roles.SCOPE_STORE_CORE), STORE)).isTrue();
        }

        @Test
        void aHumanStillReachesEntitlementsThroughTheOrdinaryStoreRead() {
            assertThat(checker.hasAccessOnBillingEntitlementRead(staff(Roles.ROLE_STORE_ADMIN), STORE)).isTrue();
            assertThat(checker.hasAccessOnBillingEntitlementRead(staff(Roles.ROLE_CUSTOMER), STORE)).isFalse();
        }

        @Test
        void theQuotaCheckIsAServiceToServiceCallOnly() {
            assertThat(checker.hasAccessOnBillingQuotaCheck(service(Roles.SCOPE_STORE_CORE))).isTrue();
            assertThat(checker.hasAccessOnBillingQuotaCheck(superAdmin())).isFalse();
            assertThat(checker.hasAccessOnBillingQuotaCheck(orgAdmin(ORG))).isFalse();
        }
    }

    @Nested
    class ThePodRegistry {

        @Test
        void readingAdmitsTheOperatorTheOrgAdminAndTheGatewayServicePrincipal() {
            assertThat(checker.hasAccessOnPodRead(superAdmin())).isTrue();
            assertThat(checker.hasAccessOnPodRead(orgAdmin(ORG))).isTrue();
            assertThat(checker.hasAccessOnPodRead(service(Roles.SCOPE_STORE_CORE))).isTrue();
            assertThat(checker.hasAccessOnPodRead(staff(Roles.ROLE_STORE_ADMIN))).isFalse();
        }

        @Test
        void managingAPodIsThePlatformOperatorsAloneBecauseDeletingOneOrphansItsStores() {
            assertThat(checker.hasAccessOnPodManage(superAdmin())).isTrue();
            assertThat(checker.hasAccessOnPodManage(orgAdmin(ORG))).isFalse();
            assertThat(checker.hasAccessOnPodManage(service(Roles.SCOPE_STORE_CORE))).isFalse();
        }

        @Test
        void placementIsAskedBeforeTheStoreExistsSoOnlyAServicePrincipalAsks() {
            assertThat(checker.hasAccessOnPodPlacement(service(Roles.SCOPE_STORE_CORE))).isTrue();
            assertThat(checker.hasAccessOnPodPlacement(superAdmin())).isFalse();
        }
    }

    @Nested
    class Shoppers {

        @Test
        void aCustomerIsRecognisedOnlyInItsOwnStore() {
            assertThat(checker.isCustomerInSameStore(staff(Roles.ROLE_CUSTOMER), OTHER_STORE)).isFalse();
            assertThat(checker.isCustomerInSameStore(staff(Roles.ROLE_STORE_ADMIN), STORE)).isFalse();
        }
    }
}
