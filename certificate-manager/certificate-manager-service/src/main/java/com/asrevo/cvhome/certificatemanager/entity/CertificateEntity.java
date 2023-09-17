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
public class CertificateEntity extends BaseEntity<CertificateEntity, CertificateId> {
    private Instant notAfter;
    private Instant notBefore;
    private String serialNumber;
    private Integer version;
    private String sigAlgName;
    private String sigAlgOID;

    @Override
    protected CertificateId generateId() {
        return CertificateId.newId();
    }
}
