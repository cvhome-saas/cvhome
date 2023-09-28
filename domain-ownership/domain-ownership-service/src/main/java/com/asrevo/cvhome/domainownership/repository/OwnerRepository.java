package com.asrevo.cvhome.domainownership.repository;

import com.asrevo.cvhome.domainownership.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.commons.domain.OwnerId;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, OwnerId> {
    Optional<OwnerEntity> findByIdentity(IdentityId identityId);
}
