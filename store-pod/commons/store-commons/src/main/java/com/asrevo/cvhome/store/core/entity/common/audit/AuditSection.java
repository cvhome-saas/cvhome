package com.asrevo.cvhome.store.core.entity.common.audit;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

@Embeddable
@Setter
@Getter
public class AuditSection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "DATE_CREATED")
    private Instant dateCreated;

    @Column(name = "DATE_MODIFIED")
    private Instant dateModified;

    @Getter
    @Column(name = "UPDT_ID", length = 60)
    private String modifiedBy;


    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

}
