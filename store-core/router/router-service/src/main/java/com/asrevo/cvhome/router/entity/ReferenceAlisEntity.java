package com.asrevo.cvhome.router.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.router.commons.domain.ReferenceAlisId;
import com.asrevo.cvhome.router.commons.domain.ReferenceId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("reference_alis")
public class ReferenceAlisEntity extends BaseEntity<ReferenceAlisEntity, ReferenceAlisId> {
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private Domain alis;
    @Column("reference_id")
    private AggregateReference<ReferenceEntity, ReferenceId> reference;

    public static ReferenceAlisEntity create(Domain alis) {
        ReferenceAlisEntity entity = new ReferenceAlisEntity();
        entity.setNew();
        entity.setAlis(alis);
        return entity;
    }

    @Override
    protected ReferenceAlisId generateId() {
        return ReferenceAlisId.newId();
    }
}

