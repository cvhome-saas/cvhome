package com.asrevo.cvhome.tenancy.manager.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.tenancy.manager.service.impl.InternalStoreServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tenant isolation for stores, enforced in the query rather than only by {@code @PreAuthorize}.
 *
 * <p>
 * The permission gate cannot carry this on its own: the shared {@code StoreRoleAccessChecker.isOrgAdmin} ignores
 * the store it is handed and returns true for any store on the platform once the caller is an org admin. Tenancy
 * owns {@code manager_store.org_id}, so these are the checks that actually hold.
 * </p>
 */
@Tag("unit-test")
class StoreTenantScopingTest {

    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");

    private static final ManagerOrgId OTHER_ORG = new ManagerOrgId("31f023932bc66470c104b770");

    private static final StoreMerchantId STORE = new StoreMerchantId("507f1f77bcf86cd799439011");

    private static final PodId POD = new PodId("607f1f77bcf86cd799439022");

    private ManagerStoreRepository repository;

    private InternalStoreServiceImpl service;

    private static UserOrgStoreIdentity identity(ManagerOrgId org, Roles... roles) {
        return new UserOrgStoreIdentity(org, new StoreMerchantId("*"), Set.of(roles));
    }

    private static ManagerStoreEntity storeOwnedBy(ManagerOrgId org) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setOrgId(org);
        entity.setName("a-store");
        return entity;
    }

    /** The org id the list query was actually given — null means "every org". */
    private String capturedQueryOrg() {
        ArgumentCaptor<String> org = ArgumentCaptor.forClass(String.class);
        verify(repository).findVisible(org.capture(), any(), any(), any(), anyInt(), anyLong());
        return org.getValue();
    }

    @BeforeEach
    void setUp() {
        repository = mock(ManagerStoreRepository.class);
        ManagerStoreMappers mappers = mock(ManagerStoreMappers.class);
        service = new InternalStoreServiceImpl(repository, mock(ManagerOrgRepository.class), mappers,
                mock(ExternalEntitlementService.class));
    }

    private void givenNoRows() {
        when(repository.findVisible(any(), any(), any(), any(), anyInt(), anyLong())).thenReturn(List.of());
        when(repository.countVisible(any(), any(), any(), any())).thenReturn(0L);
    }

    @Test
    @DisplayName("a principal with an org but no recognised role is still confined to its org")
    void unrecognisedRoleIsStillScoped() {
        givenNoRows();

        // ROLE_CUSTOMER matches none of the org/store-admin branches the old code keyed off, so it fell through
        // and was handed every store on the platform.
        service.findAll(identity(ORG, Roles.ROLE_CUSTOMER), new ListManagerStoreQuery(null, null, null, null),
                Pageable.ofSize(10));

        assertThat(capturedQueryOrg()).isEqualTo(ORG.id().toString());
    }

    @Test
    @DisplayName("an org admin is confined to its org")
    void orgAdminIsScoped() {
        givenNoRows();

        service.findAll(identity(ORG, Roles.ROLE_ORG_ADMIN), new ListManagerStoreQuery(null, null, null, null),
                Pageable.ofSize(10));

        assertThat(capturedQueryOrg()).isEqualTo(ORG.id().toString());
    }

    @Test
    @DisplayName("a platform-wide caller (super admin / store-core token) is not scoped")
    void platformWideIsUnscoped() {
        givenNoRows();

        // getOrgStoreIdentity reports both a super admin and a store_core service token with a null org.
        service.findAll(identity(null, Roles.ROLE_SUPER_ADMIN), new ListManagerStoreQuery(null, null, null, null),
                Pageable.ofSize(10));

        assertThat(capturedQueryOrg()).isNull();
    }

    /** The pod id the list query was given — null means "every pod". */
    private String capturedQueryPod() {
        ArgumentCaptor<String> pod = ArgumentCaptor.forClass(String.class);
        verify(repository).findVisible(any(), any(), any(), pod.capture(), anyInt(), anyLong());
        return pod.getValue();
    }

    @Test
    @DisplayName("a pod filter reaches the query as the pod's id")
    void podFilterIsApplied() {
        givenNoRows();

        service.findAll(identity(null, Roles.ROLE_SUPER_ADMIN), new ListManagerStoreQuery(null, null, null, POD),
                Pageable.ofSize(10));

        assertThat(capturedQueryPod()).isEqualTo(POD.getId().toString());
    }

    @Test
    @DisplayName("no pod filter leaves the query unfiltered by pod")
    void withoutAPodTheQueryIsUnfiltered() {
        givenNoRows();

        service.findAll(identity(null, Roles.ROLE_SUPER_ADMIN), new ListManagerStoreQuery(null, null, null, null),
                Pageable.ofSize(10));

        assertThat(capturedQueryPod()).isNull();
    }

    /*
     * The property that matters: a pod filter narrows what the caller may already see. If it replaced the org
     * scoping instead of composing with it, an org admin could read every tenant's stores by naming a pod.
     */
    @Test
    @DisplayName("a pod filter composes with org scoping rather than replacing it")
    void podFilterDoesNotWidenScope() {
        givenNoRows();

        service.findAll(identity(ORG, Roles.ROLE_ORG_ADMIN), new ListManagerStoreQuery(null, null, null, POD),
                Pageable.ofSize(10));

        ArgumentCaptor<String> org = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> pod = ArgumentCaptor.forClass(String.class);
        verify(repository).findVisible(org.capture(), any(), any(), pod.capture(), anyInt(), anyLong());
        assertThat(org.getValue()).isEqualTo(ORG.id().toString());
        assertThat(pod.getValue()).isEqualTo(POD.getId().toString());
    }

    @Test
    @DisplayName("reading another org's store is refused as 404, not 403 — a 403 would confirm the id exists")
    void foreignStoreIsRefused() {
        when(repository.findById(STORE)).thenReturn(Optional.of(storeOwnedBy(OTHER_ORG)));

        assertThatThrownBy(() -> service.findStore(identity(ORG, Roles.ROLE_ORG_ADMIN), STORE))
                .isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    @DisplayName("reading a store in the caller's own org is allowed")
    void ownStoreIsAllowed() throws StoreNotFoundException {
        when(repository.findById(STORE)).thenReturn(Optional.of(storeOwnedBy(ORG)));

        service.findStore(identity(ORG, Roles.ROLE_ORG_ADMIN), STORE);

        verify(repository).findById(STORE);
    }

    @Test
    @DisplayName("a missing store is a 404, not the NoSuchElementException 500 it used to be")
    void missingStoreIsNotFound() {
        when(repository.findById(STORE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findStore(identity(ORG, Roles.ROLE_ORG_ADMIN), STORE))
                .isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    @DisplayName("a platform-wide caller may read any org's store")
    void platformWideMayReadAnyStore() throws StoreNotFoundException {
        when(repository.findById(STORE)).thenReturn(Optional.of(storeOwnedBy(OTHER_ORG)));

        service.findStore(identity(null, Roles.ROLE_SUPER_ADMIN), STORE);

        verify(repository).findById(STORE);
    }

}
