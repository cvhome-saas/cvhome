package com.asrevo.cvhome.store.core.entity.common.audit;

import java.time.Instant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class AuditListener {

    @PrePersist
    public void onSave(Object o) {
        if (o instanceof Auditable audit) {
            AuditSection auditSection = audit.getAuditSection();

            auditSection.setDateModified(Instant.now());
            if (auditSection.getDateCreated() == null) {
                auditSection.setDateCreated(Instant.now());
            }
            audit.setAuditSection(auditSection);
        }
    }

    @PreUpdate
    public void onUpdate(Object o) {
        if (o instanceof Auditable audit) {
            AuditSection auditSection = audit.getAuditSection();

            auditSection.setDateModified(Instant.now());
            if (auditSection.getDateCreated() == null) {
                auditSection.setDateCreated(Instant.now());
            }
            audit.setAuditSection(auditSection);
        }
    }

}
