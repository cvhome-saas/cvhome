package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class InternalStoreServiceImpl implements InternalStoreService {
    private final ManagerStoreRepository storeRepository;
    private final ManagerStoreMappers storeMappers;

    @Transactional
    @Override
    public ManagerStoreDto createStore(
            CreateManagerStoreRequest storeRequest, IdentityId identityId) {
        ManagerStoreEntity entity =
                storeRepository.save(ManagerStoreEntity.createStore(storeRequest, identityId));
        return storeMappers.toDto(entity);
    }

    @Override
    public Page<ManagerStoreDto> findAll(
            UserOrgStoreIdentity identityInfo,
            ListManagerStoreQuery listManagerStoreQuery,
            Pageable pageable) {
        ManagerStoreEntity entity = storeMappers.toEntity(listManagerStoreQuery);
        if (identityInfo.isOrgAdminOrAnyStoreAdmin()) {
            entity.setOwner(identityInfo.org());
        }
        if (identityInfo.isAnyStoreAdmin()) {
            entity.setId(new ManagerStoreId(identityInfo.store()));
        }

        entity.setSyncedInRouter(Boolean.TRUE);
        entity.setSyncedInStore(Boolean.TRUE);
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(
                all.stream().map(storeMappers::toDto).toList(),
                all.getPageable(),
                all.getTotalElements());
    }

    @Transactional
    @Override
    public void syncInRouter(ManagerStoreId store) {
        ManagerStoreEntity storeEntity = getManagerStoreEntity(store);
        storeEntity.syncInRouter();
        storeRepository.save(storeEntity);
    }

    @Transactional
    @Override
    public void syncInStore(ManagerStoreId store) {
        ManagerStoreEntity storeEntity = getManagerStoreEntity(store);
        storeEntity.syncInStore();
        storeRepository.save(storeEntity);
    }

    private ManagerStoreEntity getManagerStoreEntity(ManagerStoreId store) {
        return storeRepository
                .findById(store)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Override
    public IdentityId getStoreOwner(ManagerStoreId store) {
        return getManagerStoreEntity(store).getOwner();
    }

    @Override
    public ManagerStoreDto findStore(ManagerStoreId store) {
        return storeMappers.toDto(getManagerStoreEntity(store));
    }

    @Override
    public Boolean checkNameExists(String name) {
        return storeRepository.existsByName(name);
    }
}
