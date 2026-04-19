package com.asrevo.cvhome.store.core.entity.common.audit;

public interface Auditable {

    AuditSection getAuditSection();

    void setAuditSection(AuditSection audit);

}
