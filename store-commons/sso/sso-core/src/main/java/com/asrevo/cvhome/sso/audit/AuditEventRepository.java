package com.asrevo.cvhome.sso.audit;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** Reads and appends; the one delete is retention's. */
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long>,
        JpaSpecificationExecutor<AuditEventEntity> {

    @Modifying
    @Query("delete from AuditEventEntity e where e.occurredAt < :before")
    int deleteOlderThan(Instant before);

}
