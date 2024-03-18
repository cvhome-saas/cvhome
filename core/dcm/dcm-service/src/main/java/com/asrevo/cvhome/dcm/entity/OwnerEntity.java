package com.asrevo.cvhome.dcm.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.DomainId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.data.relational.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Table("owner")
public class OwnerEntity extends BaseEntity<OwnerEntity, IdentityId> {
    @Embedded(onEmpty = USE_NULL)
    private Email email;
    @MappedCollection(idColumn = "owner_id")
    private Set<OwnerDomainRefEntity> domains;

    public static OwnerEntity create(Email email, IdentityId ownerId) {
        OwnerEntity entity = new OwnerEntity();
        entity.setNew();
        entity.setId(ownerId);
        entity.setEmail(email);
        entity.setDomains(new HashSet<>());
        return entity;
    }

    @Override
    protected IdentityId generateId() {
        return this.id;
    }

    public OwnerEntity addToDomains(DomainId domainId) {
        if (this.domains == null || this.domains.isEmpty()) this.domains = new HashSet<>();
        OwnerDomainRefEntity ref = new OwnerDomainRefEntity();
        ref.setDomain(AggregateReference.to(domainId));
        this.domains.add(ref);
        return this;
    }
}
