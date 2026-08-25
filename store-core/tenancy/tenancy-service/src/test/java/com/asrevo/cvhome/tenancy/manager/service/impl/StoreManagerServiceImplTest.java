package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaRequest;
import com.asrevo.cvhome.billing.services.quota.ExternalStoreQuotaService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.services.placement.ExternalPodPlacementService;
import com.asrevo.cvhome.tenancy.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.DuplicateStoreNameException;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StorePodClientFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Creating a store, and what a listing does when a pod cannot answer for one.
 *
 * <p>
 * Two properties are pinned here. Creation <em>fails closed</em> on billing — refusing one store is recoverable by
 * retrying, while a store that exists with nobody billed for it is not noticed until someone reconciles revenue.
 * Listing <em>fails open</em> on the pod — a row that cannot be decorated is still shown, because the version that
 * returned {@code null} and filtered made stores disappear from the console, and a merchant looking at a list with a
 * store missing concludes it was deleted.
 * </p>
 */
class StoreManagerServiceImplTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final PodId PREFERRED_POD = new PodId("507f1f77bcf86cd799439012");

    private static final UserOrgStoreIdentity IDENTITY =
            new UserOrgStoreIdentity(ORG, new StoreMerchantId("*"), Set.of(Roles.ROLE_ORG_ADMIN));

    private static final String STORE_NAME = "a-store";

    private static final String CHOSEN = "chosen";

    private static final String QUOTA_REASON = "plan limit";

    private static final String CODE = "code";

    private static final String STORE_CODE = "ORG1-STORE1";

    private InternalStoreService internalStoreService;

    private StorePodClientFactory podClientFactory;

    private ExternalPodPlacementService placementService;

    private ExternalStoreQuotaService quotaService;

    private StoreManagerServiceImpl service;

    @BeforeEach
    void setUp() {
        internalStoreService = mock(InternalStoreService.class);
        podClientFactory = mock(StorePodClientFactory.class);
        placementService = mock(ExternalPodPlacementService.class);
        quotaService = mock(ExternalStoreQuotaService.class);
        ManagerStoreMappers mappers = mock(ManagerStoreMappers.class);
        when(mappers.toPage(any(), any()))
                .thenAnswer(it -> new PageImpl<>(it.getArgument(0), PageRequest.of(0, 20), 1));
        service = new StoreManagerServiceImpl(internalStoreService, mappers, podClientFactory, placementService,
                quotaService);
    }

    private static CreateStoreRequest request(String preferredPod) {
        CreateStoreRequest request = new CreateStoreRequest();
        request.setName(STORE_NAME);
        if (preferredPod != null) {
            request.setPod(new CreateStoreRequest.PodRef(preferredPod));
        }
        return request;
    }

    private static ManagerStoreDto store() {
        return new ManagerStoreDto(STORE, STORE_NAME, ORG, POD, ProvisioningState.NOT_STARTED_PROVISIONING,
                StoreStatus.ACTIVE, null, null);
    }

    private void billingAllows() throws Exception {
        when(quotaService.checkStoreCreate(any())).thenReturn(StoreQuotaDecision.allow(true, 0));
    }

    @Test
    void aStoreIsCreatedOnThePodTheRegistryChose() throws Exception {
        billingAllows();
        when(placementService.place(any())).thenReturn(new PlacementDecision(POD, false, CHOSEN));
        when(internalStoreService.createStore(any(), any(), any())).thenReturn(store());

        assertThat(service.createStore(ORG, request(null)).podId()).isEqualTo(POD);

        verify(internalStoreService).createStore(any(), org.mockito.ArgumentMatchers.eq(ORG),
                org.mockito.ArgumentMatchers.eq(POD));
    }

    /** An operator's pod preference is passed to the registry, which is free to honour it or not. */
    @Test
    void aPreferredPodIsForwardedToTheRegistry() throws Exception {
        billingAllows();
        when(placementService.place(any())).thenReturn(new PlacementDecision(POD, false, CHOSEN));
        when(internalStoreService.createStore(any(), any(), any())).thenReturn(store());

        service.createStore(ORG, request(PREFERRED_POD.id().toString()));

        verify(placementService).place(new PlacementRequest(ORG, PREFERRED_POD));
    }

    @Test
    void noPreferenceReachesTheRegistryAsNoPreference() throws Exception {
        billingAllows();
        when(placementService.place(any())).thenReturn(new PlacementDecision(POD, false, CHOSEN));
        when(internalStoreService.createStore(any(), any(), any())).thenReturn(store());

        service.createStore(ORG, request("   "));

        verify(placementService).place(new PlacementRequest(ORG, null));
    }

    /** The billing check runs before a pod is chosen, so a refusal costs nothing. */
    @Test
    void aQuotaRefusalStopsBeforeAPodIsEvenChosen() throws Exception {
        when(quotaService.checkStoreCreate(any())).thenReturn(StoreQuotaDecision.refuse(QUOTA_REASON, false, 3));

        assertThatThrownBy(() -> service.createStore(ORG, request(null)))
                .hasMessageContaining(QUOTA_REASON);

        verify(placementService, never()).place(any());
        verify(internalStoreService, never()).createStore(any(), any(), any());
    }

    @Test
    void theQuotaIsAskedAboutTheCallersOwnOrganization() throws Exception {
        billingAllows();
        when(placementService.place(any())).thenReturn(new PlacementDecision(POD, false, CHOSEN));
        when(internalStoreService.createStore(any(), any(), any())).thenReturn(store());

        service.createStore(ORG, request(null));

        verify(quotaService).checkStoreCreate(new StoreQuotaRequest(ORG));
    }

    /**
     * The unique constraint is the authority, not the read-then-write name check: two concurrent creates both pass
     * that. The violation is translated out here, outside {@code createStore}'s transaction — catching inside would
     * trade it for an {@code UnexpectedRollbackException} at commit, still a 500 and minus the cause.
     */
    @Test
    void aNameCollisionOnTheConstraintBecomesATypedConflict() throws Exception {
        billingAllows();
        when(placementService.place(any())).thenReturn(new PlacementDecision(POD, false, CHOSEN));
        when(internalStoreService.createStore(any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("manager_store_name_uq"));

        assertThatThrownBy(() -> service.createStore(ORG, request(null)))
                .isInstanceOf(DuplicateStoreNameException.class);
    }

    @Test
    void aStoreDetailCarriesThePodItWasFetchedFrom() throws Exception {
        MerchantStorePodClient client = mock(MerchantStorePodClient.class);
        when(internalStoreService.getStorePod(STORE)).thenReturn(POD);
        when(podClientFactory.getMerchantStorePodClient(POD)).thenReturn(client);
        when(client.getStore(STORE.getId().toString())).thenReturn(Map.of(CODE, STORE_CODE));

        Object detail = service.getStore(STORE);

        assertThat(detail).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                .containsEntry(CODE, STORE_CODE)
                .containsEntry("pod", Map.of("id", POD.id()));
    }

    /** Entering a store is refused for a suspended one, and the check runs before the pod is even resolved. */
    @Test
    void anInoperableStoreIsNeverFetchedFromItsPod() throws Exception {
        org.mockito.Mockito.doThrow(StoreNotFoundException.of(STORE)).when(internalStoreService)
                .requireOperable(STORE);

        assertThatThrownBy(() -> service.getStore(IDENTITY, STORE)).isInstanceOf(StoreNotFoundException.class);

        verify(podClientFactory, never()).getMerchantStorePodClient(any());
    }

    /**
     * A pod that is slow or down degrades the row to what tenancy knows. It used to be
     * {@code catch (Exception e) { return null; }} followed by a filter, so the store vanished from the console
     * instead.
     */
    @Test
    void aPodThatCannotAnswerLeavesTheRowInTheListing() {
        when(internalStoreService.findAll(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(store()), PageRequest.of(0, 20), 1));
        when(podClientFactory.getMerchantStorePodClient(any()))
                .thenThrow(new IllegalStateException("pod unreachable"));

        var page = service.findAll(IDENTITY, null, PageRequest.of(0, 20));

        assertThat(page.getContent()).containsExactly(store());
    }

    @Test
    void aReachablePodDecoratesTheRow() throws Exception {
        MerchantStorePodClient client = mock(MerchantStorePodClient.class);
        when(internalStoreService.findAll(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(store()), PageRequest.of(0, 20), 1));
        when(internalStoreService.getStorePod(STORE)).thenReturn(POD);
        when(podClientFactory.getMerchantStorePodClient(POD)).thenReturn(client);
        when(client.getStore(STORE.getId().toString())).thenReturn(Map.of(CODE, STORE_CODE));

        var page = service.findAll(IDENTITY, null, PageRequest.of(0, 20));

        assertThat(page.getContent().getFirst()).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.MAP)
                .containsEntry(CODE, STORE_CODE);
    }

}
