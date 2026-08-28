package com.asrevo.cvhome.catalog.config;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.guard.StoreEntitlements;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The layer that actually refuses a write to a lapsed store.
 *
 * <p>
 * Reads are deliberately untouched — a seller whose payment has lapsed must still be able to see their catalog and
 * their shop must still serve it — so the interesting cases here are the ones that are let through.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class StoreBillingWriteGateTest {

    private static final String STORE_ID = "65f023632bc46470c104b76f";

    private static final String PATH = "/api/v1/private/category";

    private static final String STORE_PARAM = "store";

    @Mock
    private ExternalEntitlementService entitlementService;

    private StoreBillingWriteGate.Interceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new StoreBillingWriteGate.Interceptor(
                new StoreEntitlements(entitlementService, Duration.ofMinutes(1)));
    }

    private void operable(boolean operable) throws Exception {
        when(entitlementService.snapshot(any())).thenReturn(new EntitlementSnapshot(
                new StoreMerchantId(STORE_ID),
                operable ? SubscriptionStatus.ACTIVE : SubscriptionStatus.CANCELED, operable, "plan", null,
                Map.of()));
    }

    private static MockHttpServletRequest request(HttpMethod method, String store) {
        MockHttpServletRequest request = new MockHttpServletRequest(method.name(), PATH);
        if (store != null) {
            request.setParameter(STORE_PARAM, store);
        }
        return request;
    }

    @Test
    void aWriteToALapsedStoreIsRefusedWithPaymentRequired() throws Exception {
        operable(false);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean proceed = interceptor.preHandle(request(HttpMethod.POST, STORE_ID), response, null);

        assertThat(proceed).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.PAYMENT_REQUIRED.value());
        assertThat(response.getContentAsString()).contains("BILLING.STORE.SUSPENDED");
        assertThat(response.getContentType()).startsWith("application/problem+json");
    }

    @Test
    void everyReadOfALapsedStoreStillGoesThrough() throws Exception {
        // Not a convenience: taking the shop offline is what turns a recoverable billing problem into a lost
        // customer, so only changing things is refused.
        for (HttpMethod method : new HttpMethod[]{HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS}) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertThat(interceptor.preHandle(request(method, STORE_ID), response, null)).isTrue();
            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }
    }

    @Test
    void aWriteToAPayingStoreGoesThrough() throws Exception {
        operable(true);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request(HttpMethod.PUT, STORE_ID), response, null)).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void aRequestWithoutAStoreIsNoneOfThisGatesBusiness() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        // there is nothing to ask billing about, and refusing here would break every unscoped write
        assertThat(interceptor.preHandle(request(HttpMethod.POST, null), response, null)).isTrue();
    }

}
