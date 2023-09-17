package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.CertificateId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("certificate")
public class CertificateEntity extends BaseEntity<CertificateId> {
    private String location;
    private Instant createdAt;
    private Instant validTo;

    @Override
    protected CertificateId generateId() {
        return CertificateId.newId();
    }
}
