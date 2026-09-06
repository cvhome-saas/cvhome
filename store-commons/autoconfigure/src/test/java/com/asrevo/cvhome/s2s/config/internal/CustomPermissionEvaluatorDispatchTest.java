package com.asrevo.cvhome.s2s.config.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.services.PermissionAccessChecker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The permission dispatch table itself: which checker answers which token.
 *
 * <p>
 * This switch is the reason AGENTS.md lists "a new permission token with no case in CustomPermissionEvaluator" as
 * reject-on-sight — the default arm denies, so a token nobody wired up produces a silent 403 that looks like a
 * misconfigured role. It is also where a token can be wired to the <em>wrong</em> checker, which is worse: the
 * endpoint answers, to the wrong audience. Every token is asserted to reach the method it is meant to, by
 * replacing the checker with a mock and watching which call arrives.
 * </p>
 */
class CustomPermissionEvaluatorDispatchTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String TARGET_TYPE = "StoreMerchantId";
    private static final String ORG = "21f023932bc66470c104b76f";

    private final PermissionAccessChecker checker = Mockito.mock(PermissionAccessChecker.class);
    private final Authentication authentication = Mockito.mock(Authentication.class);
    private CustomPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new CustomPermissionEvaluator(new StaticApplicationContext());
        // The evaluator builds its own checker from the context; swapping it out is what makes the dispatch visible.
        ReflectionTestUtils.setField(evaluator, "checker", checker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STORE-POD.MERCHANT.*", "STORE-POD.CONTENT.*", "STORE-POD.CATALOG.*",
        "STORE-POD.CHECKOUT.*", "STORE-POD.CUA.*", "STORE-POD.PAYMENT.*", "STORE-POD.INVENTORY.*"})
    void everyPodManageTokenAsksForManageAccess(String token) {
        when(checker.hasManageAccessOnStore(any(), any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, token)).isTrue();

        verify(checker).hasManageAccessOnStore(authentication, STORE, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STORE-POD.MERCHANT.READ", "STORE-POD.CONTENT.READ"})
    void theTwoPodReadTokensAskForReadAccessRatherThanManage(String token) {
        when(checker.hasReadAccessOnStore(any(), any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, token)).isTrue();

        // Wiring a read token to the manage checker would 403 every moderator; the reverse would let one write.
        verify(checker).hasReadAccessOnStore(authentication, STORE, null);
        verify(checker, Mockito.never()).hasManageAccessOnStore(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"STORE-POD.CATALOG.RESERVE", "STORE-POD.INVENTORY.RESERVE",
        "STORE-POD.CHECKOUT.SIGNAL", "STORE-POD.CONTENT.MEDIA-USAGE"})
    void theServiceToServiceTokensAskOnlyThatTheCallerIsOnTheSamePod(String token) {
        when(checker.isSameStorePod(any(), any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, token)).isTrue();

        verify(checker).isSameStorePod(authentication, STORE, null);
    }

    @Test
    void theStoreCreateTokenTakesAnOrgIdRatherThanAStoreId() {
        when(checker.hasAccessOnStoreCreate(any(), any(), any())).thenReturn(true);

        // The store does not exist yet, so the target is the organisation that would own it.
        assertThat(evaluator.hasPermission(authentication, ORG, TARGET_TYPE,
                "STORE-POD.MERCHANT.STORE-CREATE")).isTrue();

        verify(checker).hasAccessOnStoreCreate(authentication, ORG, null);
    }

    @Test
    void theCustomerTokenAsksTheShopperQuestionAndNothingElse() {
        when(checker.isCustomerInSameStore(any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, "STORE-POD.CUSTOMER.*")).isTrue();

        verify(checker).isCustomerInSameStore(authentication, STORE);
    }

    @ParameterizedTest
    @CsvSource({"STORE-CORE.BILLING.READ,hasAccessOnBillingRead", "STORE-CORE.BILLING.MANAGE,hasAccessOnBillingManage",
        "STORE-CORE.BILLING.ENTITLEMENT-READ,hasAccessOnBillingEntitlementRead"})
    void eachBillingTokenReachesItsOwnCheckerAndNotItsNeighbour(String token, String expected) {
        when(checker.hasAccessOnBillingRead(any(), any())).thenReturn(true);
        when(checker.hasAccessOnBillingManage(any(), any())).thenReturn(true);
        when(checker.hasAccessOnBillingEntitlementRead(any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, token)).isTrue();

        switch (expected) {
            case "hasAccessOnBillingRead" -> verify(checker).hasAccessOnBillingRead(authentication, STORE);
            case "hasAccessOnBillingManage" -> verify(checker).hasAccessOnBillingManage(authentication, STORE);
            default -> verify(checker).hasAccessOnBillingEntitlementRead(authentication, STORE);
        }
    }

    @Test
    void theTwoServiceOnlyTokensTakeNoStoreAtAll() {
        when(checker.hasAccessOnBillingQuotaCheck(any())).thenReturn(true);
        when(checker.hasAccessOnPodPlacement(any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, null, TARGET_TYPE, "STORE-CORE.BILLING.QUOTA-CHECK"))
                .isTrue();
        assertThat(evaluator.hasPermission(authentication, null, TARGET_TYPE, "STORE-CORE.POD.PLACEMENT")).isTrue();

        verify(checker).hasAccessOnBillingQuotaCheck(authentication);
        verify(checker).hasAccessOnPodPlacement(authentication);
    }

    @ParameterizedTest
    @CsvSource({"STORE-CORE.POD.READ,read", "STORE-CORE.POD.MANAGE,manage"})
    void thePodRegistryTokensAreDistinctBecauseReadingAndDeletingAPodAreNot(String token, String expected) {
        when(checker.hasAccessOnPodRead(any())).thenReturn(true);
        when(checker.hasAccessOnPodManage(any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, null, TARGET_TYPE, token)).isTrue();

        if ("read".equals(expected)) {
            verify(checker).hasAccessOnPodRead(authentication);
            verify(checker, Mockito.never()).hasAccessOnPodManage(any());
        } else {
            verify(checker).hasAccessOnPodManage(authentication);
        }
    }

    @Test
    void theStoreFindOneTokenReachesTheStoreRead() {
        when(checker.hasAccessOnStoreFindOne(any(), any())).thenReturn(true);

        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, "STORE-CORE.STORE-FIND-ONE")).isTrue();

        verify(checker).hasAccessOnStoreFindOne(authentication, STORE);
    }

    @Test
    void aTokenNobodyWiredUpDeniesRatherThanFallingThrough() {
        // This is the failure AGENTS.md names: it looks like a misconfigured role and is a missing case.
        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, "STORE-POD.SOMETHING.NEW")).isFalse();
        assertThat(evaluator.hasPermission(authentication, STORE, TARGET_TYPE, "STORE-CORE.SOMETHING.NEW")).isFalse();
        Mockito.verifyNoInteractions(checker);
    }

    @Test
    void theThreeArgumentOverloadIsNeverUsedAndAlwaysDenies() {
        // Spring's PermissionEvaluator has two shapes; every @PreAuthorize here names the four-argument one.
        assertThat(evaluator.hasPermission(authentication, STORE, "STORE-POD.CATALOG.*")).isFalse();
    }
}
