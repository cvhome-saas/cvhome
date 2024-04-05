package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
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
    public ManagerStoreDto createStore(CreateManagerStoreRequest storeRequest, IdentityId identityId) {
        ManagerStoreEntity entity = storeRepository.save(ManagerStoreEntity.createStore(storeRequest, identityId));
        return storeMappers.toDto(entity);
    }

    @Override
    public Page<ManagerStoreDto> findAll(ListManagerStoreQuery listManagerStoreQuery, IdentityId identityId, Pageable pageable) {
        ManagerStoreEntity entity = storeMappers.toEntity(listManagerStoreQuery);
        entity.setOwner(identityId);
        entity.setSyncedInRouter(Boolean.TRUE);
        entity.setSyncedInStore(Boolean.TRUE);
        Page<ManagerStoreEntity> all = storeRepository.findAll(Example.of(entity), pageable);
        return new PageImpl<>(all.stream().map(storeMappers::toDto).toList(), all.getPageable(), all.getTotalElements());
    }

    @Transactional
    @Override
    public void syncInRouter(ManagerStoreId storeId) {
        ManagerStoreEntity storeEntity = getManagerStoreEntity(storeId);
        storeEntity.syncInRouter();
        storeRepository.save(storeEntity);
    }

    @Transactional
    @Override
    public void syncInStore(ManagerStoreId storeId) {
        ManagerStoreEntity storeEntity = getManagerStoreEntity(storeId);
        storeEntity.syncInStore();
        storeRepository.save(storeEntity);
    }

    private ManagerStoreEntity getManagerStoreEntity(ManagerStoreId storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
    }

    @Override
    public IdentityId getStoreOwner(ManagerStoreId storeId) {
        return getManagerStoreEntity(storeId).getOwner();
    }

    @Override
    public ManagerStoreDto findStore(ManagerStoreId storeId) {
        return storeMappers.toDto(getManagerStoreEntity(storeId));
    }
}
