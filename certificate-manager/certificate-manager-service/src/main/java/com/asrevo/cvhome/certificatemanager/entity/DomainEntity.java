package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.DomainId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

@Getter
@Setter
@Table("domain")
public class DomainEntity extends BaseEntity<DomainId> {
    private String domain;
    private String status;
    private boolean autoRenew;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainCertificateRefEntity> certificates;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainOrdersRefEntity> orders;
    @Column("active_certificate_id")
    private AggregateReference<CertificateEntity, CertificateId> activeCertificateId;

    @Override
    protected DomainId generateId() {
        return DomainId.newId();
    }
}
