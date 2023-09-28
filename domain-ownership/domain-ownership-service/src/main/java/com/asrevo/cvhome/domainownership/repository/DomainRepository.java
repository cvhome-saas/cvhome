package com.asrevo.cvhome.domainownership.repository;

import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.domainownership.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.domain.DomainEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface DomainRepository extends ListCrudRepository<DomainEntity, DomainId> {
    Optional<DomainEntity> findByDomain(Domain domain);
}
