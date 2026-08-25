package com.asrevo.cvhome.billing.guard;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The guard every pod enforces a plan's ceilings through.
 *
 * <p>
 * It sits on write paths in catalog, merchant and checkout, so its failure behaviour is the whole design: it never
 * throws for an unreachable billing service, because a caller on a write path needs an answer and "billing is
 * unavailable" is not one it can act on. It falls back to the last answer it saw, and to permissive if it has never
 * seen one — the opposite choice is made at store creation, where being unable to check means refusing.
 * </p>
 */
class StoreEntitlementsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private ExternalEntitlementService remote;

    private StoreEntitlements guard;

    @BeforeEach
    void setUp() {
        remote = mock(ExternalEntitlementService.class);
        guard = new StoreEntitlements(remote, Duration.ofSeconds(30L));
    }

    private static EntitlementSnapshot snapshot(SubscriptionStatus status, boolean operable,
                                                Map<EntitlementKey, EntitlementValue> entitlements) {
        return new EntitlementSnapshot(STORE, status, operable, "PRO", null, entitlements);
    }

    private static BillingApiUnavailableException unavailable() {
        return BillingApiUnavailableException.from(new RemoteErrorContext(null, null, Map.of(), List.of(),
                "billing", 0, null, new java.net.ConnectException("refused")));
    }

    // ------------------------------------------------------------------------------------------------ caching

    @Test
    @DisplayName("a snapshot is fetched once and served from memory within the window")
    void cachesTheSnapshot() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true, Map.of()));

        guard.snapshot(STORE);
        guard.snapshot(STORE);

        // Asked on every pod write path; without the cache each product save would be an extra HTTP round trip.
        verify(remote, times(1)).snapshot(any());
    }

    @Test
    @DisplayName("the cache key is the store id, not the object identity of whatever was passed in")
    void cacheKeyIsTheStoreId() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true, Map.of()));

        guard.snapshot(STORE);
        guard.snapshot(new StoreMerchantId(STORE.storeMerchantId()));

        verify(remote, times(1)).snapshot(any());
    }

    // ----------------------------------------------------------------------------------------------- fallback

    @Test
    @DisplayName("billing going down falls back to the last answer seen for that store")
    void fallsBackToTheLastKnownAnswer() throws Exception {
        when(remote.snapshot(any()))
                .thenReturn(snapshot(SubscriptionStatus.SUSPENDED, false, Map.of()))
                .thenThrow(unavailable());
        guard.snapshot(STORE);
        StoreEntitlements fresh = new StoreEntitlements(remote, Duration.ZERO);

        // A separate guard with no live cache, driven by the same remote, would go permissive; this one remembers.
        assertThat(guard.snapshot(STORE).operable()).isFalse();
        assertThat(fresh.snapshot(STORE).operable()).isTrue();
    }

    @Test
    @DisplayName("a store never seen before, with billing down, is allowed through")
    void goesPermissiveForAnUnknownStore() throws Exception {
        when(remote.snapshot(any())).thenThrow(unavailable());

        EntitlementSnapshot result = guard.snapshot(STORE);

        // An outage in billing must not stop a paying merchant from taking orders.
        assertThat(result.operable()).isTrue();
        assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.planCode()).isNull();
        assertThat(result.entitlements()).isEmpty();
    }

    @Test
    @DisplayName("the guard never throws, whatever billing does")
    void neverThrows() throws Exception {
        when(remote.snapshot(any())).thenThrow(new IllegalStateException("something unexpected"));

        assertThatCode(() -> guard.snapshot(STORE)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("operable is the one question every enforcement layer asks")
    void operableDelegatesToTheSnapshot() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.SUSPENDED, false, Map.of()));

        assertThat(guard.operable(STORE)).isFalse();
    }

    // -------------------------------------------------------------------------------------------- ceilings

    @Test
    @DisplayName("a store below its ceiling passes")
    void underTheCeiling() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.MAX_PRODUCTS, EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500))));

        assertThatCode(() -> guard.require(STORE, EntitlementKey.MAX_PRODUCTS, () -> 499))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a store at its ceiling is refused, because one more would be too many")
    void atTheCeiling() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.MAX_PRODUCTS, EntitlementValue.limit(EntitlementKey.MAX_PRODUCTS, 500))));

        assertThatThrownBy(() -> guard.require(STORE, EntitlementKey.MAX_PRODUCTS, () -> 500))
                .isInstanceOf(EntitlementExceededException.class);
    }

    @Test
    @DisplayName("the count is never asked for when no ceiling applies")
    void theCountIsDeferred() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true, Map.of()));
        AtomicInteger asked = new AtomicInteger();

        guard.require(STORE, EntitlementKey.MAX_PRODUCTS, () -> {
            asked.incrementAndGet();
            return 1_000_000;
        });

        // Counting is often the expensive part — a select count(*) over a tenant's whole catalog — so it sits behind
        // a supplier and is never evaluated for a plan with no ceiling on that key.
        assertThat(asked.get()).isZero();
    }

    @Test
    @DisplayName("a plan that does not mention a key does not accidentally forbid it")
    void anAbsentKeyIsUnlimited() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true, Map.of()));

        assertThatCode(() -> guard.require(STORE, EntitlementKey.MAX_ORDERS_MONTH, () -> 999_999))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a capability key is never treated as a ceiling")
    void aCapabilityKeyIsNotACeiling() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.ANALYTICS, EntitlementValue.flag(EntitlementKey.ANALYTICS, false))));

        assertThatCode(() -> guard.require(STORE, EntitlementKey.ANALYTICS, () -> 5)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a ceiling of zero forbids the first one, which is not the same as unlimited")
    void aZeroCeilingForbids() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.MAX_ACCOUNTS, EntitlementValue.limit(EntitlementKey.MAX_ACCOUNTS, 0))));

        assertThatThrownBy(() -> guard.require(STORE, EntitlementKey.MAX_ACCOUNTS, () -> 0))
                .isInstanceOf(EntitlementExceededException.class);
    }

    // ------------------------------------------------------------------------------------------ capabilities

    @Test
    @DisplayName("a capability the plan grants passes")
    void aGrantedCapability() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.CUSTOM_DOMAIN, EntitlementValue.flag(EntitlementKey.CUSTOM_DOMAIN, true))));

        assertThatCode(() -> guard.requireGranted(STORE, EntitlementKey.CUSTOM_DOMAIN))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("a capability the plan withholds is refused")
    void aWithheldCapability() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true,
                Map.of(EntitlementKey.CUSTOM_DOMAIN, EntitlementValue.flag(EntitlementKey.CUSTOM_DOMAIN, false))));

        assertThatThrownBy(() -> guard.requireGranted(STORE, EntitlementKey.CUSTOM_DOMAIN))
                .isInstanceOf(EntitlementExceededException.class);
    }

    @Test
    @DisplayName("a capability the plan never mentions is refused, because a grant has to be explicit")
    void anUnmentionedCapability() throws Exception {
        when(remote.snapshot(any())).thenReturn(snapshot(SubscriptionStatus.ACTIVE, true, Map.of()));

        // The asymmetry with a ceiling is deliberate: an absent number means no cap, an absent capability means it
        // was not sold.
        assertThatThrownBy(() -> guard.requireGranted(STORE, EntitlementKey.PRIORITY_SUPPORT))
                .isInstanceOf(EntitlementExceededException.class);
    }

}
