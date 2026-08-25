package com.asrevo.cvhome.tenancy.manager.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.tenancy.commons.dto.AuditEntityType;
import com.asrevo.cvhome.tenancy.commons.dto.AuditSource;
import com.asrevo.cvhome.tenancy.manager.entity.TenancyAuditEntity;
import com.asrevo.cvhome.tenancy.manager.repository.TenancyAuditRepository;

import lombok.RequiredArgsConstructor;

/**
 * Writes the audit row for a tenancy mutation.
 *
 * <p>
 * Called from the service layer inside the same transaction as the change, so the record and the fact it records
 * commit together — an audit written afterwards is one that can be missing precisely when something went wrong.
 * Never called from a controller: the controller does not know what the previous state was.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class TenancyAuditService {

    private final TenancyAuditRepository auditRepository;

    public void record(AuditEntityType type, Object entityId, String action, Object from, Object to, String actor,
                       String detail) {
        auditRepository.save(
                TenancyAuditEntity.of(type, entityId, action, from, to, actor, AuditSource.API, detail));
    }

    /** For changes nobody asked for — a reaper, a scheduled sweep. */
    public void recordJob(AuditEntityType type, Object entityId, String action, Object from, Object to,
                          String detail) {
        auditRepository.save(
                TenancyAuditEntity.of(type, entityId, action, from, to, "system", AuditSource.JOB, detail));
    }

}
