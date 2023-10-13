package com.asrevo.cvhome.domaincertificatemanager.entity;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.*;
import com.asrevo.cvhome.domaincertificatemanager.commons.event.domain.DomainReferenceChangedEvent;
import com.asrevo.cvhome.domaincertificatemanager.commons.event.domain.DomainRegisteredEvent;
import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
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
    @Column("owner_id")
    private AggregateReference<com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity, IdentityId> owner;

    public static DomainEntity create(Domain domain, Reference reference, DomainType domainType, IdentityId identity) {
        DomainEntity entity = new DomainEntity();
        entity.setNew();
        entity.setDomain(domain);
        entity.setReference(reference);
        entity.setDomainType(domainType);
        entity.setStatus(DomainCertificateStatus.INITIATED);
        entity.setAutoOrder(true);
        entity.setAutoRenew(true);
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
