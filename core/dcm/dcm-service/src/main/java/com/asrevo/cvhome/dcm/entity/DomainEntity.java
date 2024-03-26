package com.asrevo.cvhome.dcm.entity;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.dcm.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.dcm.commons.event.domain.DomainReferenceChangedEvent;
import com.asrevo.cvhome.dcm.commons.event.domain.DomainRegisteredEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("domain")
public class DomainEntity extends BaseEntity<DomainEntity, DomainId> {
    @Embedded(onEmpty = USE_NULL)
    private Domain domain;
    @Embedded(onEmpty = USE_NULL)
    private Reference reference;
    @Column("domain_type")
    private DomainType domainType;
    private DomainCertificateStatus status;
    @Column("generated_date")
    private Instant generatedDate;
    private boolean autoRenew;
    private boolean autoOrder;
    private boolean includeSubDomains;
    @Column("owner_id")
    private AggregateReference<OwnerEntity, IdentityId> owner;

    public static DomainEntity create(Domain domain, Reference reference, DomainType domainType, boolean includeSubDomains, IdentityId identity) {
        DomainEntity entity = new DomainEntity();
        entity.setNew();
        entity.setDomain(domain);
        entity.setReference(reference);
        entity.setDomainType(domainType);
        entity.setStatus(DomainCertificateStatus.INITIATED);
        entity.setAutoOrder(!DomainType.SAS_SUB.equals(domainType));
        entity.setAutoRenew(!DomainType.SAS_SUB.equals(domainType));
        entity.setIncludeSubDomains(includeSubDomains);
        entity.setOwner(AggregateReference.to(identity));
        entity.registerEvent(DomainRegisteredEvent.from(entity.getId(), identity, domain, entity.autoRenew, entity.autoOrder));
        return entity;
    }

    @Override
    protected DomainId generateId() {
        return DomainId.newId();
    }

    public void changeDomainReference(Reference reference) {
        this.registerEvent(DomainReferenceChangedEvent.from(this.getId(), this.getDomain(), this.reference, reference));
        this.setReference(reference);

    }
}
