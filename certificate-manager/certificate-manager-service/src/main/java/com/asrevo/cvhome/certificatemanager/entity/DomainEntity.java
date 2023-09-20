package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.event.domain.DomainRegisteredEvent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Set;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("domain")
public class DomainEntity extends BaseEntity<DomainEntity, DomainId> {
    @NotNull
    @Embedded(onEmpty = USE_NULL)
    private Domain domain;
    private String status;
    private boolean autoRenew;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainCertificateRefEntity> certificates;
    @MappedCollection(idColumn = "domain_id")
    private Set<DomainOrdersRefEntity> orders;
    @Column("active_certificate_id")
    private AggregateReference<CertificateEntity, CertificateId> activeCertificateId;

    public static DomainEntity createDomain(Domain domain, boolean autoRenew) {
        DomainEntity entity = new DomainEntity();
        entity.setNew();
        entity.setAutoRenew(autoRenew);
        entity.registerEvent(DomainRegisteredEvent.from(domain));
        return entity;
    }

    @Override
    protected DomainId generateId() {
        return DomainId.newId();
    }
}
