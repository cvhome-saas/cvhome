package com.asrevo.cvhome.podregistry.api.v1;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementDecision;
import com.asrevo.cvhome.podregistry.commons.dto.PlacementRequest;
import com.asrevo.cvhome.podregistry.domain.PodStorePlacementEntity;
import com.asrevo.cvhome.podregistry.service.PodCapacityService;
import com.asrevo.cvhome.podregistry.service.PodLifecycleService;
import com.asrevo.cvhome.podregistry.service.PodPlacementService;
import com.asrevo.cvhome.podregistry.service.PodService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The registry's endpoints, and the one decision they make rather than delegate.
 *
 * <p>
 * {@code isPlatformWide} is that decision, and it is a tenant-isolation control wearing a helper's clothes: a
 * principal whose token carries an org sees only that org's pods, while one without an org sees every pod on the
 * platform. Getting it backwards — or letting a null org fall through as "no filter" by accident rather than by
 * intent — hands one operator another organisation's private infrastructure.
 * </p>
 */
class PodApisTest {

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");
    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");
    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String TERM = "pod";
    private static final String OPERATOR = "ops@example.com";

    private final PodService podService = Mockito.mock(PodService.class);
    private final PodLifecycleService lifecycleService = Mockito.mock(PodLifecycleService.class);
    private final PodPlacementService placementService = Mockito.mock(PodPlacementService.class);
    private final PodCapacityService capacityService = Mockito.mock(PodCapacityService.class);

    private final PodApi podApi = new PodApi(podService, lifecycleService);
    private final PodPlacementApi placementApi = new PodPlacementApi(placementService, capacityService);

    private static Pod pod() {
        return new Pod(POD, "pod-a", new PodEndpoint("http://pod.example", EndpointType.EXTERNAL), null, null);
    }

    private static UserOrgStoreIdentity identity(ManagerOrgId org) {
        return new UserOrgStoreIdentity(org, STORE, Set.of(Roles.ROLE_ORG_ADMIN));
    }

    @Test
    void aPrincipalCarryingAnOrgSeesOnlyThatOrgsPods() {
        when(podService.listAllPods(eq(ORG), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(pod())));

        assertThat(podApi.listPods(identity(ORG))).hasSize(1);

        verify(podService).listAllPods(eq(ORG), any(Pageable.class));
        verify(podService, Mockito.never()).listAllPods(any(Pageable.class));
    }

    @Test
    void aPlatformPrincipalSeesEveryPod() {
        when(podService.listAllPods(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(pod())));

        assertThat(podApi.listPods(null)).hasSize(1);
        assertThat(podApi.listPods(identity(null))).hasSize(1);
        assertThat(podApi.listPods(identity(new ManagerOrgId((String) null)))).hasSize(1);

        // All three shapes of "no organisation" have to mean the same thing; only one of them is a null identity.
        verify(podService, Mockito.times(3)).listAllPods(any(Pageable.class));
        verify(podService, Mockito.never()).listAllPods(any(ManagerOrgId.class), any(Pageable.class));
    }

    @Test
    void theSearchEndpointNarrowsByTheSameOwnershipRule() {
        when(podService.searchPods(any(), any(), any())).thenReturn(new PageImpl<>(List.of(pod())));

        podApi.findAllPods(identity(ORG), Pageable.unpaged(), TERM);
        podApi.findAllPods(null, Pageable.unpaged(), TERM);

        verify(podService).searchPods(eq(ORG), eq(TERM), any());
        verify(podService).searchPods(eq(null), eq(TERM), any());
    }

    @Test
    void theSingleReadCreateUpdateAndDeleteAllPassTheirIdThrough() throws Exception {
        podApi.find(POD);
        podApi.create(pod());
        podApi.update(POD, pod());
        podApi.delete(POD);

        verify(podService).view(POD);
        verify(podService).save(pod());
        verify(podService).update(POD, pod());
        verify(podService).delete(POD);
    }

    @Test
    void drainingAndResumingRecordWhoAskedForIt() throws Exception {
        Authentication operator = new UsernamePasswordAuthenticationToken(OPERATOR, null, List.of());

        podApi.drain(POD, operator);
        podApi.resume(POD, operator);

        verify(lifecycleService).drain(POD, OPERATOR);
        verify(lifecycleService).resume(POD, OPERATOR);
    }

    @Test
    void anUnauthenticatedLifecycleCallIsRecordedAsUnknownRatherThanNull() throws Exception {
        // The actor lands in an audit row; a null there is a row that says nothing about who drained a pod.
        assertThat(Mockito.mockingDetails(lifecycleService).isMock()).isTrue();

        podApi.drain(POD, null);

        verify(lifecycleService).drain(POD, "unknown");
    }

    @Test
    void placingAStoreReturnsTheDecisionAndLogsItForBothOwnedAndSharedRequests() throws Exception {
        PlacementDecision decision = new PlacementDecision(POD, false, "least-loaded public pod");
        when(placementService.place(any())).thenReturn(decision);

        // Both request shapes go through the same log line, and the org-less one dereferences a null org there.
        assertThat(placementApi.place(new PlacementRequest(ORG))).isEqualTo(decision);
        assertThat(placementApi.place(new PlacementRequest(null, POD))).isEqualTo(decision);

        verify(placementService).place(new PlacementRequest(ORG));
        verify(placementService).place(new PlacementRequest(null, POD));
    }

    @Test
    void placementDelegatesAndBothRecordingShapesReachCapacity() throws Exception {
        placementApi.recordPlacement(null);
        placementApi.recordPlacements(List.of());

        verify(capacityService).recordPlacement(null);
        verify(capacityService).recordPlacements(List.of());
    }

    @Test
    void aPlacementRowStampsWhenTheStoreLanded() {
        Instant before = Instant.now();

        PodStorePlacementEntity placement = PodStorePlacementEntity.of(STORE, POD);

        assertThat(placement.getStoreId()).isEqualTo(STORE);
        assertThat(placement.getPodId()).isEqualTo(POD);
        assertThat(placement.getPlacedAt()).isBetween(before, Instant.now());
    }
}
