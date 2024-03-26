package com.asrevo.cvhome.dcm.entity;

import com.asrevo.cvhome.dcm.commons.domain.CertificateId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("domain_certificate_ref")
@Getter
@Setter
public class DomainCertificateRefEntity {
    @Column("certificate_id")
    private AggregateReference<CertificateEntity, CertificateId> certificate;
}
