package com.asrevo.cvhome.domainownership.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
