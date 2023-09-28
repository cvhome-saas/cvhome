package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.repository.DomainRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DomainServiceImpl implements DomainService {
    private final DomainRepository domainRepository;

    @Override
    public DomainEntity save(DomainEntity entity) {
        return domainRepository.save(entity);
    }

    @Override
    public DomainEntity findOneByDomain(Domain domain) {
        return domainRepository.findOneByDomain(domain);
    }
}
