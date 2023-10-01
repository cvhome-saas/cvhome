package com.asrevo.cvhome.certificatemanager.repository;

import com.asrevo.cvhome.certificatemanager.domain.OwnerEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
