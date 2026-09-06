package com.asrevo.cvhome.gateway.client;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.services.entitlement.ReactiveExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The blocked set fails open: a billing outage keeps the last known set rather than clearing it, and a gateway that has
 * never heard from billing blocks nothing.
 */
class StoreBillingStatusClientTest {

    private static final String STORE_A = "65f023632bc46470c104b76f";

    private static final String STORE_B = "65f023632bc46470c104b75f";

    private final ReactiveExternalEntitlementService entitlementService = mock(ReactiveExternalEntitlementService.class);

    private final StoreBillingStatusClient client = new StoreBillingStatusClient(entitlementService);

    @Test
    void blocksNothingBeforeTheFirstRefresh() {
        assertThat(client.blocked(STORE_A)).isFalse();
        assertThat(client.blocked(null)).isFalse();
    }

    @Test
    void refreshReplacesTheSetWholesale() {
        when(entitlementService.blockedStores()).thenReturn(Mono.just(List.of(new StoreMerchantId(STORE_A))));
        client.refresh().block();
        assertThat(client.blocked(STORE_A)).isTrue();
        assertThat(client.blocked(STORE_B)).isFalse();

        when(entitlementService.blockedStores()).thenReturn(Mono.just(List.of(new StoreMerchantId(STORE_B))));
        client.refresh().block();
        assertThat(client.blocked(STORE_A)).isFalse();
        assertThat(client.blocked(STORE_B)).isTrue();
    }

    @Test
    void failedRefreshKeepsTheLastKnownSet() {
        when(entitlementService.blockedStores()).thenReturn(Mono.just(List.of(new StoreMerchantId(STORE_A))));
        client.refresh().block();

        when(entitlementService.blockedStores()).thenReturn(Mono.error(new IllegalStateException("billing is down")));
        client.refresh().block();

        assertThat(client.blocked(STORE_A)).isTrue();
    }

}
