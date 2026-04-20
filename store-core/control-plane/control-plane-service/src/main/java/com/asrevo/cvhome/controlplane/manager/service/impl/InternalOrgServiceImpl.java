package com.asrevo.cvhome.controlplane.manager.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.controlplane.manager.entity.ManagerOrgEntity;
import com.asrevo.cvhome.controlplane.manager.mappers.ManagerOrgMappers;
import com.asrevo.cvhome.controlplane.manager.repository.ManagerOrgRepository;
import com.asrevo.cvhome.controlplane.manager.service.InternalOrgService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class InternalOrgServiceImpl implements InternalOrgService {

    private final ManagerOrgRepository managerOrgRepository;

    private final ManagerOrgMappers managerOrgMappers;

    @Transactional
    @Override
    public ManagerOrgId createOrgForUser(Email email) {
        ManagerOrgEntity entity = managerOrgRepository.save(ManagerOrgEntity.createOrgFromUser(email));
        return entity.getId();
    }

    @Override
    public Page<ManagerOrgDto> findAll(Pageable pageable) {
        return managerOrgMappers.map(managerOrgRepository.findAll(pageable));
    }

    @Override
    public ManagerOrgDto findOne(ManagerOrgId id) {
        return managerOrgRepository.findById(id).map(managerOrgMappers::toDto).orElseThrow();
    }

}
