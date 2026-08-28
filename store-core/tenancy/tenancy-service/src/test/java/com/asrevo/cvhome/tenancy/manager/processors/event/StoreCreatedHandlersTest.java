package com.asrevo.cvhome.tenancy.manager.processors.event;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.ProvisionSubscriptionRequest;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;
import com.asrevo.cvhome.errors.UncheckedBaseException;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.podregistry.api.errors.PodRegistryUnavailableException;
import com.asrevo.cvhome.podregistry.commons.dto.RecordPlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent;
import com.asrevo.cvhome.tenancy.manager.service.StoreProvisioningService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The three handlers a created store fans out to, and the one decision they each have to get right: which failures
 * the outbox should retry.
 *
 * <p>
 * The outbox writes one record per handler, so pod provisioning, billing and the registry's capacity count retry
 * independently — a registry that is down cannot stop a store being built or billed. Within each handler the split
 * is the same: <em>unreachable</em> propagates, because the work still needs doing; <em>refused</em> is swallowed,
 * because retrying an identical request is refused identically forever and would burn the record's attempts and
 * bury the one line an operator has to read.
 * </p>
 */
class StoreCreatedHandlersTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final StoreCreatedEvent EVENT =
            new StoreCreatedEvent(STORE, ORG, POD, Map.of(), new CreateStoreRequest());

    private static final String BILLING = "billing";

    private static RemoteErrorContext unreachable(String service) {
        return new RemoteErrorContext(null, "no answer", null, null, service, 0, null, null);
    }

    private static RemoteErrorContext refused(String service, String code) {
        return new RemoteErrorContext(code, "refused", Map.of(), List.of(), service, 422, null, null);
    }

    @Nested
    class BillingProvisioning {

        private ExternalStoreQuotaService billing;

        private BillingProvisioningEventImpl handler;

        @BeforeEach
        void setUp() {
            billing = mock(ExternalStoreQuotaService.class);
            handler = new BillingProvisioningEventImpl(billing);
        }

        @Test
        void aStoreIsProvisionedAgainstTheOrganizationThatOwnsIt() throws Exception {
            // Built before the outer stubbing rather than inside it: stubbing one mock while another's `when` is
            // still open is what Mockito reports as an unfinished stubbing.
            SubscriptionView subscription = subscription();
            when(billing.provision(any())).thenReturn(subscription);

            handler.process(EVENT);

            verify(billing).provision(new ProvisionSubscriptionRequest(ORG, STORE));
        }

        /** A refusal will be refused identically forever, so retrying it only buries the operator's one line. */
        @Test
        void aRefusalIsRecordedAndNotRetried() throws Exception {
            when(billing.provision(any()))
                    .thenThrow(StoreQuotaRefusedException.from(refused(BILLING, "BILLING.QUOTA.REFUSED")));

            assertThatCode(() -> handler.process(EVENT)).doesNotThrowAnyException();
        }

        /** Billing was down, not unwilling — the store still needs its subscription, so the outbox must retry. */
        @Test
        void anUnreachableBillingServicePropagatesSoTheOutboxRetries() throws Exception {
            when(billing.provision(any())).thenThrow(BillingApiUnavailableException.from(unreachable(BILLING)));

            assertThatThrownBy(() -> handler.process(EVENT)).isInstanceOf(UncheckedBaseException.class);
        }

        private static SubscriptionView subscription() {
            SubscriptionView view = mock(SubscriptionView.class);
            when(view.status()).thenReturn(SubscriptionStatus.ACTIVE);
            return view;
        }

    }

    @Nested
    class PodProvisioning {

        private StoreProvisioningService provisioning;

        private ManagerStoreCreatedEventImpl handler;

        @BeforeEach
        void setUp() {
            provisioning = mock(StoreProvisioningService.class);
            handler = new ManagerStoreCreatedEventImpl(provisioning);
        }

        @Test
        void theEventCarriesEverythingTheProvisionerNeeds() throws Exception {
            handler.process(EVENT);

            verify(provisioning).provisioning(ORG, STORE, POD, EVENT.request());
        }

        /** The row is gone, so there is nothing to build and no retry that would help. */
        @Test
        void aStoreThatVanishedIsAbandonedRatherThanRetried() throws Exception {
            doThrow(StoreNotFoundException.of(STORE)).when(provisioning).provisioning(any(), any(), any(), any());

            assertThatCode(() -> handler.process(EVENT)).doesNotThrowAnyException();
        }

        @Test
        void anUnreachablePodPropagatesSoTheOutboxRetries() throws Exception {
            doThrow(RemoteServiceUnavailableException.of("merchant", Map.of(), null)).when(provisioning)
                    .provisioning(any(), any(), any(), any());

            assertThatThrownBy(() -> handler.process(EVENT)).isInstanceOf(UncheckedBaseException.class);
        }

    }

    @Nested
    class PodCapacity {

        private ExternalPodPlacementService placement;

        private PodCapacityEventImpl handler;

        @BeforeEach
        void setUp() {
            placement = mock(ExternalPodPlacementService.class);
            handler = new PodCapacityEventImpl(placement);
        }

        /**
         * Counted from the committed event rather than folded into the placement call that chose the pod: creation
         * can still fail after placement, and counting there leaks capacity on every abandoned attempt.
         */
        @Test
        void thePlacementIsRecordedAgainstTheStoreAndItsPod() throws Exception {
            handler.process(EVENT);

            verify(placement).recordPlacement(new RecordPlacementRequest(STORE, POD));
        }

        /** A miscounted pod misplaces later stores, and the endpoint is idempotent, so a retry costs nothing. */
        @Test
        void anUnreachableRegistryPropagatesSoTheOutboxRetries() throws Exception {
            doThrow(PodRegistryUnavailableException.from(unreachable("pod-registry"))).when(placement)
                    .recordPlacement(any());

            assertThatThrownBy(() -> handler.process(EVENT)).isInstanceOf(UncheckedBaseException.class);
        }

    }

}
