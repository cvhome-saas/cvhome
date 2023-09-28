package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;

public interface DomainService {
    DomainEntity save(DomainEntity entity);

    DomainEntity findOneByDomain(Domain domain);
}
