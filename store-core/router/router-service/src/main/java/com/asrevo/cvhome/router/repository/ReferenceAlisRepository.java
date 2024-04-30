package com.asrevo.cvhome.router.repository;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.router.commons.domain.ReferenceAlisId;
import com.asrevo.cvhome.router.entity.ReferenceAlisEntity;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ReferenceAlisRepository extends ListCrudRepository<ReferenceAlisEntity, ReferenceAlisId> {
    Optional<ReferenceAlisEntity> findByAlis(Domain domain);
}
