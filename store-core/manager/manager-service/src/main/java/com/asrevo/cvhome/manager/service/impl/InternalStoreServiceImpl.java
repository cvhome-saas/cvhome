package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.commons.dto.ProvisioningState;
import com.asrevo.cvhome.manager.dto.StoreDomainList;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.utils.Defines;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import com.asrevo.cvhome.s2s.model.AppProperties;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalStoreServiceImpl implements InternalStoreService {
    private final ManagerStoreRepository storeRepository;
    private final ServiceDomainProperties serviceDomainProperties;
    private final AppProperties appProperties;
    private final ManagerStoreMappers storeMappers;

    @Transactional
    @Override
    public ManagerStoreDto createStore(Map<Object, Object> request, ManagerOrgId orgId, PodId podId) {
        ManagerStoreEntity entity = storeRepository.save(ManagerStoreEntity.createStore(request, orgId, podId));
        return storeMappers.toDto(entity);
    }

    @Transactional
    @Override
    public void completeProvisioning(ManagerStoreId store) {
        storeRepository.findById(store)
                .map(it -> storeRepository.save(it.completeProvisioning()));

    }

    @Transactional
    @Override
    public void failProvisioning(ManagerStoreId store) {
        storeRepository.findById(store)
                .map(it -> storeRepository.save(it.failProvisioning()));

    }

    @Transactional
    @Override
    public void startProvisioning(ManagerStoreId store) {
        storeRepository.findById(store)
                .map(it -> storeRepository.save(it.startProvisioning()));

    }

    @Override
    public Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identityInfo, ListManagerStoreQuery listManagerStoreQuery, Pageable pageable) {
        ManagerStoreEntity entity = storeMappers.toEntity(listManagerStoreQuery);
        if (identityInfo.isOrgAdminOrAnyStoreAdmin()) {
            entity.setOrgId(new ManagerOrgId(identityInfo.org().id()));
        }
        if (identityInfo.isAnyStoreAdmin()) {
            entity.setId(new ManagerStoreId(identityInfo.store()));
        }
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(all.stream().map(storeMappers::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

    @Override
    public Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setOrgId(new ManagerOrgId(id.id()));
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(all.stream().map(storeMappers::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

    private ManagerStoreEntity getManagerStoreEntity(ManagerStoreId store) {
        return storeRepository.findById(store)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Override
    public ManagerOrgId getStoreOwner(ManagerStoreId store) {
        return getManagerStoreEntity(store).getOrgId();
    }

    @Override
    public ManagerStoreDto findStore(ManagerStoreId store) {
        return storeMappers.toDto(getManagerStoreEntity(store));
    }

    @Override
    public Boolean checkNameExists(String name) {
        return storeRepository.existsByName(name);
    }

    @Override
    public Pod getStorePod(ManagerStoreId managerStoreId) {
        ManagerStoreEntity store = getManagerStoreEntity(managerStoreId);
        return serviceDomainProperties.getPodByPodId(store.getPodId()).orElseThrow(() -> new OperationExecution(ErrorCodes.store_pod_not_match_any));
    }

    @Override
    public StoreDomainList domains(ManagerStoreId managerStoreId) {
        return storeRepository.findById(managerStoreId)
                .map(ManagerStoreEntity::domains)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Override
    public ManagerStoreId getReferenceByDomain(Domain domain) {
        return storeRepository.findByDomain(domain.domain(), appProperties.getDomain(), Defines.SAAS_POD_SUFFIX)
                .map(BaseEntity::getId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Transactional
    @Override
    public void addDomain(ManagerStoreId managerStoreId, Domain domain) {
        storeRepository.findById(managerStoreId)
                .map(it -> it.addDomain(domain))
                .map(storeRepository::save)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Transactional
    @Override
    public void removeDomain(ManagerStoreId managerStoreId, Domain domain) {
        storeRepository.findById(managerStoreId)
                .map(it -> it.removeDomain(domain))
                .map(storeRepository::save)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

}
