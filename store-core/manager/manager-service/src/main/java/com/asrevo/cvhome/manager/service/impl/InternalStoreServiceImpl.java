package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.manager.entity.ManagerStoreEntity;
import com.asrevo.cvhome.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.manager.repository.ManagerStoreRepository;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.utils.ErrorCodes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.List;

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
        return storeMappers.toStoreDto(entity);
    }

    @Override
    public List<ManagerStoreDto> findAll(IdentityId identityId, Pageable pageable) {
        return storeMappers.toStoreDto(storeRepository.findAllByOwner(identityId));
    }

    @Transactional
    @Override
    public void syncInRouter(ManagerStoreId storeId) {
        ManagerStoreEntity storeEntity = storeRepository.findById(storeId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
        storeEntity.syncInRouter();
        storeRepository.save(storeEntity);
    }

    @Transactional
    @Override
    public void syncInStore(ManagerStoreId storeId) {
        ManagerStoreEntity storeEntity = storeRepository.findById(storeId)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.store_not_found));
        storeEntity.syncInStore();
        storeRepository.save(storeEntity);
    }
}
