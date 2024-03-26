package com.asrevo.cvhome.dcm.repository;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.dcm.entity.OwnerEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
