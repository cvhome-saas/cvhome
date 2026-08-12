package com.asrevo.cvhome.tenancy.manager.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.billing.api.errors.BillingApiException;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.services.entitlement.ExternalEntitlementService;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.tenancy.errors.StoreNotFoundException;
import com.asrevo.cvhome.tenancy.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.tenancy.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.tenancy.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalStoreServiceImpl implements InternalStoreService {

    private final ManagerStoreRepository storeRepository;

    private final ManagerStoreMappers storeMappers;

    private final ExternalEntitlementService entitlementService;

    @Transactional
    @Override
    public ManagerStoreDto createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId podId) {
        ManagerStoreEntity entity = storeRepository.save(ManagerStoreEntity.createStore(request, orgId, podId));
        return storeMappers.toDto(entity);
    }

    @Transactional
    @Override
    public void completeProvisioning(ManagerStoreId store) {
        storeRepository.findById(store).ifPresent(it -> storeRepository.save(it.completeProvisioning()));
    }

    @Transactional
    @Override
    public void failProvisioning(ManagerStoreId store) {
        storeRepository.findById(store).ifPresent(it -> storeRepository.save(it.failProvisioning()));
    }

    @Transactional
    @Override
    public void startProvisioning(ManagerStoreId store) {
        storeRepository.findById(store).ifPresent(it -> storeRepository.save(it.startProvisioning()));
    }

    /**
     * Lists the stores the caller may see, scoped in the <em>query</em> rather than by the permission gate alone.
     *
     * <p>
     * The scoping is driven by whether the identity carries an organization, not by which roles it holds. It used to
     * key off {@code isOrgAdminOrAnyStoreAdmin()}, which fails open: a principal holding some <em>other</em> role
     * carried an org claim, matched none of those branches, and so was handed the unfiltered list of every store on
     * the platform. Absence of a recognised role has to mean less access, never more.
     * </p>
     *
     * <p>
     * A null org means the caller is platform-wide — a super admin or a {@code store_core} service token, both of
     * which {@code getOrgStoreIdentity} reports that way and both of which the endpoint's own guard has already
     * checked. Every other caller is confined to its own org, and store-level roles additionally to their one store.
     * </p>
     */
    @Override
    public Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identityInfo, ListManagerStoreQuery listManagerStoreQuery,
                                         Pageable pageable) {
        ManagerStoreEntity entity = storeMappers.toEntity(listManagerStoreQuery);
        if (!isPlatformWide(identityInfo)) {
            entity.setOrgId(new ManagerOrgId(identityInfo.org().id()));
            if (identityInfo.isAnyStoreAdmin()) {
                entity.setId(new ManagerStoreId(identityInfo.store()));
            }
        }
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(withBillingStatus(all.stream().map(storeMappers::toDto).toList()), all.getPageable(),
                all.getTotalElements());
    }

    /**
     * Whether the caller sees every org's stores: a super admin or a {@code store_core} service principal, both of
     * which arrive with no org claim.
     */
    private boolean isPlatformWide(UserOrgStoreIdentity identity) {
        return identity == null || identity.org() == null || identity.org().id() == null;
    }

    @Override
    public Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setOrgId(new ManagerOrgId(id.id()));
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(withBillingStatus(all.stream().map(storeMappers::toDto).toList()), all.getPageable(),
                all.getTotalElements());
    }

    /**
     * Fills in each store's billing standing, in one call rather than one per row.
     *
     * <p>
     * Fails open on <em>any</em> billing failure, not just an unreachable one — the catch is deliberately the base
     * type. Whatever went wrong, the honest answer for a store list is "billing standing unknown"; the alternative
     * was a 502 on the console's main screen because a read that only decorates it failed.
     *
     * <p>
     * The enforcement that actually matters happens at the gateway and in the pods, not here, so degrading costs
     * nothing but a greyed-out label.
     * </p>
     * </p>
     */
    private List<ManagerStoreDto> withBillingStatus(List<ManagerStoreDto> stores) {
        if (stores.isEmpty()) {
            return stores;
        }
        Map<ManagerStoreId, SubscriptionStatus> byStore;
        try {
            byStore = entitlementService.snapshots(stores.stream().map(ManagerStoreDto::id).toList())
                    .stream()
                    .collect(Collectors.toMap(EntitlementSnapshot::store, EntitlementSnapshot::status));
        } catch (BillingApiException e) {
            log.warn("Could not read billing status for {} stores; reporting it as unknown", stores.size(), e);
            return stores;
        }
        return stores.stream().map(it -> ManagerStoreDto.billed(it, byStore.get(it.id()))).toList();
    }

    private ManagerStoreEntity getManagerStoreEntity(ManagerStoreId store) throws StoreNotFoundException {
        return storeRepository.findById(store).orElseThrow(() -> StoreNotFoundException.of(store));
    }

    /**
     * Loads a store and refuses it if it belongs to another organization.
     *
     * <p>
     * This is the guard that actually holds today. {@code @PreAuthorize} alone does not: the shared
     * {@code StoreRoleAccessChecker.isOrgAdmin} ignores the store it is asked about and returns true for any store on
     * the platform once the caller is an org admin, so every {@code hasPermission(#store,…)} on this service passes
     * for a foreign store. Tenancy owns {@code manager_store.org_id}, so it can and must check here.
     * </p>
     *
     * <p>
     * A foreign store raises the same 404 as a missing one — see {@link StoreNotFoundException} for why that is not a
     * 403.
     * </p>
     */
    private ManagerStoreEntity getManagerStoreEntity(UserOrgStoreIdentity identity, ManagerStoreId store)
            throws StoreNotFoundException {
        ManagerStoreEntity entity = getManagerStoreEntity(store);
        if (!isPlatformWide(identity) && !identity.org().equals(entity.getOrgId())) {
            log.warn("Refusing store {} to org {}: it belongs to org {}", store, identity.org().id(),
                    entity.getOrgId());
            throw StoreNotFoundException.of(store);
        }
        return entity;
    }

    @Override
    public ManagerStoreDto findStore(ManagerStoreId store) throws StoreNotFoundException {
        return storeMappers.toDto(getManagerStoreEntity(store));
    }

    @Override
    public ManagerStoreDto findStore(UserOrgStoreIdentity identity, ManagerStoreId store)
            throws StoreNotFoundException {
        return storeMappers.toDto(getManagerStoreEntity(identity, store));
    }

    @Override
    public Boolean checkNameExists(String name) {
        return storeRepository.existsByName(name);
    }

    @Override
    public PodId getStorePod(ManagerStoreId managerStoreId) throws StoreNotFoundException {
        return getManagerStoreEntity(managerStoreId).getPodId();
    }

    @Override
    public PodId getStorePod(UserOrgStoreIdentity identity, ManagerStoreId managerStoreId)
            throws StoreNotFoundException {
        return getManagerStoreEntity(identity, managerStoreId).getPodId();
    }

}
