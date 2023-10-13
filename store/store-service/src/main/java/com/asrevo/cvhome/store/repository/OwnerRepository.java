package com.asrevo.cvhome.store.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.entity.OwnerEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
