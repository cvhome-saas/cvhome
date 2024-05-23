package com.asrevo.cvhome.manager.repository;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.domain.ReferenceAlisId;
import com.asrevo.cvhome.manager.entity.ReferenceAlisEntity;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ReferenceAlisRepository extends ListCrudRepository<ReferenceAlisEntity, ReferenceAlisId> {
    Optional<ReferenceAlisEntity> findByAlis(Domain domain);

    List<ReferenceAlisEntity> findByReference(ManagerStoreId store);

    @Query("delete from manager.reference_alis  where reference_id=:reference and domain=:domain")
    @Modifying
    void deleteDomain(String domain, String reference);
}
