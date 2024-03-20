package com.asrevo.cvhome.router.repository;

import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.router.commons.domain.ReferenceId;
import com.asrevo.cvhome.router.entity.PodEntity;
import com.asrevo.cvhome.router.entity.ReferenceEntity;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReferenceRepository extends ListCrudRepository<ReferenceEntity, ReferenceId> {
    Optional<ReferenceEntity> findByReference(Reference reference);

    @Query("""
            SELECT p.*
            FROM   reference r
                   LEFT JOIN pod p
                          ON p.id = r.pod_id
                   LEFT JOIN reference_alis ra
                          ON ra.reference_id = r.id
            WHERE  r.enabled = true
                   AND ra.domain = :domain
            LIMIT  1
            """)
    Optional<PodEntity> getAllocation(@Param("domain") String domain);

}
