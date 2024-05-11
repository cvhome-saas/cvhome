package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.entity.ReferenceAlisEntity;
import com.asrevo.cvhome.manager.repository.ReferenceAlisRepository;
import com.asrevo.cvhome.manager.service.RouterService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RouterServiceImpl implements RouterService {
    private final ReferenceAlisRepository referenceAlisRepository;

    @Transactional
    @Override
    public void create(Domain domain, ManagerStoreId managerStoreId) {
        referenceAlisRepository.save(ReferenceAlisEntity.create(domain, managerStoreId));
    }

    @Override
    public  ManagerStoreId getReferenceByDomain(Domain domain) {
        return referenceAlisRepository.findByAlis(domain).map(ReferenceAlisEntity::getReference).orElseThrow(() -> new RuntimeException(domain.domain() + " not found"));
    }
}
