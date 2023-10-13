package com.asrevo.cvhome.domaincertificatemanager.repository;

import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
import org.springframework.data.repository.ListCrudRepository;

public interface OwnerRepository extends ListCrudRepository<OwnerEntity, IdentityId> {
}
