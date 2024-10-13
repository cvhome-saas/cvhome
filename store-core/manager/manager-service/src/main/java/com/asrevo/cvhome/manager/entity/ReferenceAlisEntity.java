package com.asrevo.cvhome.manager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.domain.ReferenceAlisId;
import lombok.Getter;
import lombok.Setter;
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
    private ManagerStoreId reference;

    public static ReferenceAlisEntity create(Domain alis, ManagerStoreId reference) {
        ReferenceAlisEntity entity = new ReferenceAlisEntity();
        entity.setNew();
        entity.setAlis(alis);
        entity.setReference(reference);
        return entity;
    }

    @Override
    protected ReferenceAlisId generateId() {
        return ReferenceAlisId.newId();
    }
}
