package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import org.springframework.data.repository.CrudRepository;

public interface DomainRepository extends CrudRepository<DomainEntity, DomainId> {
    DomainEntity findOneByDomain(Domain domain);
}
