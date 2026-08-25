package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;
import com.asrevo.cvhome.tenancy.commons.dto.ProvisioningState;
import com.asrevo.cvhome.tenancy.commons.dto.StoreStatus;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.errors.StoreNotOperableException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * How a store listing is confined, and what happens when billing cannot be reached.
 *
 * <p>
 * The scoping is driven by whether the identity carries an organization, not by which roles it holds. It used to key
 * off {@code isOrgAdminOrAnyStoreAdmin()}, which fails open: a principal holding some other role carried an org
 * claim, matched none of those branches, and was handed the unfiltered list of every store on the platform. Absence
 * of a recognised role has to mean less access, never more — which is what the parameters captured below assert.
 * </p>
 */
class InternalStoreServiceImplTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final ManagerOrgId OTHER_ORG = new ManagerOrgId("352023632b046970c104b76f");

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");

    private static final int PAGE_SIZE = 25;

    /** {@code SecurityUtils}' sentinel for a principal that is not confined to one store. */
    private static final StoreMerchantId EVERY_STORE = new StoreMerchantId("*");

    private static final String POD_REFUSAL = "the pod refused it";

    private ManagerStoreRepository storeRepository;

    private ManagerOrgRepository orgRepository;

    private ExternalEntitlementService entitlementService;

    private InternalStoreServiceImpl service;

    @BeforeEach
    void setUp() {
        storeRepository = mock(ManagerStoreRepository.class);
        orgRepository = mock(ManagerOrgRepository.class);
        entitlementService = mock(ExternalEntitlementService.class);
        ManagerStoreMappers mappers = mock(ManagerStoreMappers.class);
        when(mappers.toDto(any())).thenAnswer(it -> dtoOf(it.getArgument(0)));
        service = new InternalStoreServiceImpl(storeRepository, orgRepository, mappers, entitlementService);
    }

    private static ManagerStoreDto dtoOf(ManagerStoreEntity entity) {
        return new ManagerStoreDto(entity.getId(), entity.getName(), entity.getOrgId(), entity.getPodId(),
                entity.getProvisioningState(), entity.getStatus(), null, entity.getProvisioningError());
    }

    private static ManagerStoreEntity entity(ManagerOrgId org, StoreStatus status) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setName("a-store");
        entity.setOrgId(org);
        entity.setPodId(POD);
        entity.setStatus(status);
        entity.setProvisioningState(ProvisioningState.SUCCESSFULLY_PROVISIONING);
        return entity;
    }

    private static UserOrgStoreIdentity identity(ManagerOrgId org, StoreMerchantId store, Roles role) {
        return new UserOrgStoreIdentity(org, store, Set.of(role));
    }

    /** The org and store the query was actually confined to. */
    private String[] scopingOf(UserOrgStoreIdentity identity, ListManagerStoreQuery query) {
        when(storeRepository.findVisible(any(), any(), any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        service.findAll(identity, query, PageRequest.of(0, PAGE_SIZE));
        ArgumentCaptor<String> org = ArgumentCaptor.captor();
        ArgumentCaptor<String> store = ArgumentCaptor.captor();
        ArgumentCaptor<String> pod = ArgumentCaptor.captor();
        verify(storeRepository).findVisible(org.capture(), store.capture(), any(), pod.capture(), anyInt(), anyLong());
        return new String[] {org.getValue(), store.getValue(), pod.getValue()};
    }

    @Test
    void aSuperAdminIsPlatformWideAndSoScopedToNothing() {
        assertThat(scopingOf(identity(null, EVERY_STORE, Roles.ROLE_SUPER_ADMIN), null))
                .containsExactly(null, null, null);
    }

    @Test
    void anOrgAdminIsConfinedToTheirOrganizationButNotToOneStore() {
        assertThat(scopingOf(identity(ORG, EVERY_STORE, Roles.ROLE_ORG_ADMIN), null))
                .containsExactly(ORG.id().toString(), null, null);
    }

    @Test
    void aStoreAdminIsConfinedToTheirOneStoreAsWell() {
        assertThat(scopingOf(identity(ORG, STORE, Roles.ROLE_STORE_ADMIN), null))
                .containsExactly(ORG.id().toString(), STORE.storeMerchantId(), null);
    }

    /**
     * A principal carrying an org claim but no role this method recognises must be confined by the org it carries —
     * the failing-open version handed it the platform.
     */
    @Test
    void anUnrecognisedRoleIsStillConfinedToItsOrganization() {
        assertThat(scopingOf(identity(ORG, STORE, Roles.ROLE_CUSTOMER), null))
                .containsExactly(ORG.id().toString(), null, null);
    }

    @Test
    void thePodFilterNarrowsWithinTheOrganizationScoping() {
        ListManagerStoreQuery query = new ListManagerStoreQuery(null, null, null, POD);

        assertThat(scopingOf(identity(ORG, EVERY_STORE, Roles.ROLE_ORG_ADMIN), query))
                .containsExactly(ORG.id().toString(), null, POD.id().toString());
    }

    /** An unpaged request falls back to a bounded page rather than reading the whole table. */
    @Test
    void anUnpagedRequestIsBounded() {
        when(storeRepository.findVisible(any(), any(), any(), any(), anyInt(), anyLong())).thenReturn(List.of());

        service.findAll(identity(ORG, STORE, Roles.ROLE_ORG_ADMIN), null, Pageable.unpaged());

        verify(storeRepository).findVisible(any(), any(), any(), any(), eq(100), eq(0L));
    }

    @Test
    void billingStandingIsFilledInForEveryRowInOneCall() throws Exception {
        when(storeRepository.findVisible(any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of(withId(entity(ORG, StoreStatus.ACTIVE))));
        when(entitlementService.snapshots(any()))
                .thenReturn(List.of(new EntitlementSnapshot(STORE, SubscriptionStatus.ACTIVE, true, "pro", null, null)));

        var page = service.findAll(identity(ORG, EVERY_STORE, Roles.ROLE_ORG_ADMIN), null,
                PageRequest.of(0, PAGE_SIZE));

        assertThat(page.getContent().getFirst().billingStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    /**
     * Fails open on any billing failure. The honest answer for a store list is "standing unknown"; the alternative
     * was a 502 on the console's main screen because a read that only decorates it failed.
     */
    @Test
    void aBillingFailureLeavesTheStandingUnknownRatherThanFailingTheListing() throws Exception {
        when(storeRepository.findVisible(any(), any(), any(), any(), anyInt(), anyLong()))
                .thenReturn(List.of(withId(entity(ORG, StoreStatus.ACTIVE))));
        when(entitlementService.snapshots(any())).thenThrow(BillingApiUnavailableException
                .from(new RemoteErrorContext(null, "down", null, null, "billing", 0, null, null)));

        var page = service.findAll(identity(ORG, EVERY_STORE, Roles.ROLE_ORG_ADMIN), null,
                PageRequest.of(0, PAGE_SIZE));

        assertThat(page.getContent().getFirst().billingStatus()).isNull();
    }

    /** Nothing to decorate means billing is not called at all. */
    @Test
    void anEmptyPageNeverAsksBilling() throws Exception {
        when(storeRepository.findVisible(any(), any(), any(), any(), anyInt(), anyLong())).thenReturn(List.of());

        service.findAll(identity(ORG, EVERY_STORE, Roles.ROLE_ORG_ADMIN), null,
                PageRequest.of(0, PAGE_SIZE));

        verify(entitlementService, org.mockito.Mockito.never()).snapshots(any());
    }

    /**
     * The org check lives here rather than in {@code @PreAuthorize}, because the shared {@code isOrgAdmin} ignores
     * the store it is asked about and passes for every store on the platform. A foreign store raises the same 404 as
     * a missing one, so the endpoint cannot be used to probe which ids exist.
     */
    @Test
    void aStoreOfAnotherOrganizationIsAsGoodAsMissing() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(OTHER_ORG, StoreStatus.ACTIVE)));

        assertThatThrownBy(() -> service.findStore(identity(ORG, STORE, Roles.ROLE_ORG_ADMIN), STORE))
                .isInstanceOf(StoreNotFoundException.class);
        assertThatThrownBy(() -> service.getStorePod(identity(ORG, STORE, Roles.ROLE_ORG_ADMIN), STORE))
                .isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    void aPlatformWideCallerReadsAnyOrganizationsStore() throws Exception {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(OTHER_ORG, StoreStatus.ACTIVE)));

        assertThat(service.getStorePod(identity(null, EVERY_STORE, Roles.ROLE_SUPER_ADMIN), STORE))
                .isEqualTo(POD);
    }

    @Test
    void aMissingStoreIsATypedNotFound() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findStore(STORE)).isInstanceOf(StoreNotFoundException.class);
        assertThatThrownBy(() -> service.requireOperable(STORE)).isInstanceOf(StoreNotFoundException.class);
        assertThatThrownBy(() -> service.updateStatus(STORE, StoreStatus.ACTIVE))
                .isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    void anActiveStoreOfAnActiveOrganizationIsOperable() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(ORG, StoreStatus.ACTIVE)));
        when(orgRepository.findById(ORG)).thenReturn(Optional.of(orgIn(OrgStatus.ACTIVE)));

        assertThatCode(() -> service.requireOperable(STORE)).doesNotThrowAnyException();
    }

    @Test
    void aSuspendedStoreIsNotOperable() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(ORG, StoreStatus.SUSPENDED)));

        assertThatThrownBy(() -> service.requireOperable(STORE)).isInstanceOf(StoreNotOperableException.class);
    }

    /**
     * Suspending an organization closes its stores without writing to any of them: the org owns its own status and
     * this reads both, rather than fanning out writes that drift the moment one fails.
     */
    @Test
    void anActiveStoreOwnedByASuspendedOrganizationIsNotOperable() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(ORG, StoreStatus.ACTIVE)));
        when(orgRepository.findById(ORG)).thenReturn(Optional.of(orgIn(OrgStatus.SUSPENDED)));

        assertThatThrownBy(() -> service.requireOperable(STORE)).isInstanceOf(StoreNotOperableException.class)
                .hasMessageContaining("SUSPENDED");
    }

    /** A store row with no status predates the column and is read as ACTIVE rather than as unusable. */
    @Test
    void aStoreWithNoRecordedStatusIsTreatedAsActive() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(entity(ORG, null)));
        when(orgRepository.findById(ORG)).thenReturn(Optional.empty());

        assertThatCode(() -> service.requireOperable(STORE)).doesNotThrowAnyException();
    }

    @Test
    void theProvisioningTransitionsAreWrittenBack() {
        ManagerStoreEntity row = withId(entity(ORG, StoreStatus.ACTIVE));
        when(storeRepository.findById(STORE)).thenReturn(Optional.of(row));
        when(storeRepository.save(any())).thenAnswer(it -> it.getArgument(0));

        service.startProvisioning(STORE);
        assertThat(row.getProvisioningState()).isEqualTo(ProvisioningState.IN_PROGRESS_PROVISIONING);

        service.failProvisioning(STORE, POD_REFUSAL);
        assertThat(row.getProvisioningState()).isEqualTo(ProvisioningState.FAILED_PROVISIONING);
        assertThat(row.getProvisioningError()).isEqualTo(POD_REFUSAL);

        service.completeProvisioning(STORE);
        assertThat(row.getProvisioningState()).isEqualTo(ProvisioningState.SUCCESSFULLY_PROVISIONING);
        assertThat(row.getProvisioningError()).isNull();
    }

    /** A transition for a store that no longer exists writes nothing rather than failing the outbox record. */
    @Test
    void aProvisioningTransitionForAMissingStoreIsANoOp() {
        when(storeRepository.findById(STORE)).thenReturn(Optional.empty());

        service.completeProvisioning(STORE);

        verify(storeRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void theNameCheckAsksTheUniqueConstraintsColumn() {
        when(storeRepository.existsByName(anyString())).thenReturn(true);

        assertThat(service.checkNameExists("ORG1-STORE1")).isTrue();
    }

    private static ManagerOrgEntity orgIn(OrgStatus status) {
        ManagerOrgEntity org = new ManagerOrgEntity();
        org.setStatus(status);
        return org;
    }

    /** Spring Data assigns the id on save; a hand-built entity needs one for the billing lookup to key on. */
    private static ManagerStoreEntity withId(ManagerStoreEntity entity) {
        entity.setId(STORE);
        return entity;
    }

}
