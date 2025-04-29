package com.asrevo.cvhome.store.core.entity.common.audit;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.Date;

public class AuditListener {

    @PrePersist
    public void onSave(Object o) {
        if (o instanceof Auditable audit) {
            AuditSection auditSection = audit.getAuditSection();

            auditSection.setDateModified(new Date());
            if (auditSection.getDateCreated() == null) {
                auditSection.setDateCreated(new Date());
            }
            audit.setAuditSection(auditSection);
        }
    }

    @PreUpdate
    public void onUpdate(Object o) {
        if (o instanceof Auditable audit) {
            AuditSection auditSection = audit.getAuditSection();

            auditSection.setDateModified(new Date());
            if (auditSection.getDateCreated() == null) {
                auditSection.setDateCreated(new Date());
            }
            audit.setAuditSection(auditSection);
        }
    }
}
