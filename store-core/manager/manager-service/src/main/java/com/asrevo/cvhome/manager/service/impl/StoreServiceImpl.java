package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.commons.dto.CreateStoreRequest;
import com.asrevo.cvhome.manager.commons.dto.StoreDto;
import com.asrevo.cvhome.manager.entity.StoreEntity;
import com.asrevo.cvhome.manager.mappers.StoreMappers;
import com.asrevo.cvhome.manager.repository.StoreRepository;
import com.asrevo.cvhome.manager.service.StoreService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class StoreServiceImpl implements StoreService {
    private final StoreRepository storeRepository;
    private final StoreMappers storeMappers;

    @Transactional
    @Override
    public StoreDto createStore(CreateStoreRequest storeRequest, IdentityId identityId) {
        StoreEntity entity = StoreEntity.createStore(storeRequest.name(), identityId);
        return storeMappers.toStoreDto(storeRepository.save(entity));
    }

    @Override
    public List<StoreDto> findAll(IdentityId identityId) {
        return storeMappers.toStoreDto(storeRepository.findAllByOwner(identityId));
    }

}
