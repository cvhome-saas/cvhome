package com.asrevo.cvhome.domainownership.domain;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.domainownership.commons.domain.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("owner")
public class OwnerEntity extends BaseEntity<OwnerEntity, OwnerId> {
    private IdentityId identity;
    @Embedded(onEmpty = USE_NULL)
    private Email email;
    @MappedCollection(idColumn = "owner_id")
    private Set<DomainEntity> domains;

    public static OwnerEntity create(Email email, IdentityId identity) {
        OwnerEntity entity = new OwnerEntity();
        entity.setNew();
        entity.setEmail(email);
        entity.setIdentity(identity);
        entity.setDomains(new HashSet<>());
        return entity;
    }

    @Override
    protected OwnerId generateId() {
        return OwnerId.newId();
    }

    public DomainEntity addDomain(Domain domain, Reference reference) {
        if (this.domains == null || this.domains.isEmpty()) this.domains = new HashSet<>();
        DomainEntity entity = DomainEntity.create(domain, reference);
        this.domains.add(entity);
        return entity;
    }
}
