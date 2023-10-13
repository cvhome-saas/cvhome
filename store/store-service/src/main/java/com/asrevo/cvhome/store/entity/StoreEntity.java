package com.asrevo.cvhome.store.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.asrevo.cvhome.store.commons.event.store.StoreCreatedEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("store")
public class StoreEntity extends BaseEntity<StoreEntity, StoreId> {
    @Column("name")
    String name;
    @Column("owner_id")
    private AggregateReference<OwnerEntity, IdentityId> owner;

    public static StoreEntity createStore(String name, IdentityId identityId) {
        StoreEntity entity = new StoreEntity();
        entity.setNew();
        entity.setName(name);
        entity.setOwner(AggregateReference.to(identityId));
        entity.registerEvent(StoreCreatedEvent.from(entity.getId(), identityId));
        return entity;
    }


    @Override
    protected StoreId generateId() {
        return StoreId.newId();
    }

}
