package com.asrevo.cvhome.controlplane.manager.service.impl;

import java.util.Map;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.controlplane.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.controlplane.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.controlplane.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.controlplane.manager.service.InternalStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalStoreServiceImpl implements InternalStoreService {

    private final ManagerStoreRepository storeRepository;

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

    @Override
    public Page<ManagerStoreDto> findAll(UserOrgStoreIdentity identityInfo, ListManagerStoreQuery listManagerStoreQuery,
                                         Pageable pageable) {
        ManagerStoreEntity entity = storeMappers.toEntity(listManagerStoreQuery);
        if (identityInfo.isOrgAdminOrAnyStoreAdmin()) {
            entity.setOrgId(new ManagerOrgId(identityInfo.org().id()));
        }
        if (identityInfo.isAnyStoreAdmin()) {
            entity.setId(new ManagerStoreId(identityInfo.store()));
        }
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(all.stream().map(storeMappers::toDto).toList(), all.getPageable(),
                all.getTotalElements());
    }

    @Override
    public Page<ManagerStoreDto> findAll(ManagerOrgId id, Pageable pageable) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setOrgId(new ManagerOrgId(id.id()));
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(all.stream().map(storeMappers::toDto).toList(), all.getPageable(),
                all.getTotalElements());
    }

    private ManagerStoreEntity getManagerStoreEntity(ManagerStoreId store) {
        return storeRepository.findById(store).orElseThrow();
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
    public PodId getStorePod(ManagerStoreId managerStoreId) {
        ManagerStoreEntity store = getManagerStoreEntity(managerStoreId);
        return store.getPodId();
    }

}
