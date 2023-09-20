package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.commons.domain.Domain;

public interface DomainService {
    DomainEntity save(DomainEntity entity);

    DomainEntity findOneByDomain(Domain domain);
}
