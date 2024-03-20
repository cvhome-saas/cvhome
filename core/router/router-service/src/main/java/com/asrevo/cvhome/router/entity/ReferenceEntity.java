package com.asrevo.cvhome.router.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.router.commons.domain.PodId;
import com.asrevo.cvhome.router.commons.domain.ReferenceId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Table("reference")
public class ReferenceEntity extends BaseEntity<ReferenceEntity, ReferenceId> {
    @Column("reference")
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private DomainReference reference;
    @Column("enabled")
    private Boolean enabled;
    @Column("pod_id")
    private AggregateReference<PodEntity, PodId> pod;
    @MappedCollection(idColumn = "reference_id")
    private Set<ReferenceAlisEntity> alis = new HashSet<>();

    public static ReferenceEntity create(DomainReference reference, PodId podId) {
        ReferenceEntity entity = new ReferenceEntity();
        entity.setNew();
        entity.setReference(reference);
        entity.setPod(AggregateReference.to(podId));
        entity.setEnabled(Boolean.TRUE);
        return entity;
    }

    @Override
    protected ReferenceId generateId() {
        return ReferenceId.newId();
    }

    public ReferenceEntity addAlis(ReferenceAlisEntity alis) {
        alis.setReference(AggregateReference.to(this.id));
        this.alis.add(alis);
        return this;
    }

    public ReferenceEntity enableReference() {
        this.setEnabled(Boolean.TRUE);
        return this;
    }
    public ReferenceEntity disableReference() {
        this.setEnabled(Boolean.FALSE);
        return this;
    }
}

