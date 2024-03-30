package com.asrevo.cvhome.manager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateManagerStoreRequest;
import com.asrevo.cvhome.manager.commons.event.store.StoreCreatedEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("manager_store")
public class ManagerStoreEntity extends BaseEntity<ManagerStoreEntity, ManagerStoreId> {
    @Column("name")
    private String name;
    @Column("owner_id")
    private IdentityId owner;
    @Column("created_date")
    private Instant createdDate;
    private Country country;
    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private Email email;
    private Boolean syncedInRouter;
    private Boolean syncedInStore;

    public static ManagerStoreEntity createStore(CreateManagerStoreRequest request, IdentityId identityId) {
        ManagerStoreEntity entity = new ManagerStoreEntity();
        entity.setNew();
        entity.setName(request.name());
        entity.setCreatedDate(Instant.now());
        entity.setOwner(identityId);
        entity.setEmail(request.email());
        entity.setCountry(request.country());
        entity.setSyncedInRouter(Boolean.FALSE);
        entity.setSyncedInRouter(Boolean.FALSE);
        entity.registerEvent(StoreCreatedEvent.from(entity.getId(), identityId));
        return entity;
    }


    @Override
    protected ManagerStoreId generateId() {
        return ManagerStoreId.newId();
    }

    public ManagerStoreEntity syncInRouter() {
        this.setSyncedInRouter(Boolean.TRUE);
        return this;
    }

    public ManagerStoreEntity syncInStore() {
        this.setSyncedInStore(Boolean.TRUE);
        return this;
    }
}
