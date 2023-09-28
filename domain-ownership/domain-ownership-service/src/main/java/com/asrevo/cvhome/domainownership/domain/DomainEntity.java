package com.asrevo.cvhome.domainownership.domain;

import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.domainownership.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.domain.Reference;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("domain")
public class DomainEntity extends BaseEntity<DomainEntity, DomainId> {
    @Embedded(onEmpty = USE_NULL)
    private Domain domain;
    @Embedded(onEmpty = USE_NULL)
    private Reference reference;

    public static DomainEntity create(Domain domain, Reference reference) {
        DomainEntity entity = new DomainEntity();
        entity.setNew();
        entity.setDomain(domain);
        entity.setReference(reference);
        return entity;
    }

    @Override
    protected DomainId generateId() {
        return DomainId.newId();
    }
}
