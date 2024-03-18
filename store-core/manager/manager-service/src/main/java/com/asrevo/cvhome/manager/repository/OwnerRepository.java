package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.manager.entity.OwnerEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
