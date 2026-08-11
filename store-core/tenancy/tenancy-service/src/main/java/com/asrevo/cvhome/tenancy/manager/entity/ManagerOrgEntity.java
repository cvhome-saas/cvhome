package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.events.store.OrgCreatedEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(schema = "tenancy", name = "manager_org")
public class ManagerOrgEntity extends BaseEntity<ManagerOrgEntity, ManagerOrgId> {

    @Column("created_date")
    private Instant createdDate;

    @Embedded(onEmpty = Embedded.OnEmpty.USE_NULL)
    private Email email;

    public static ManagerOrgEntity createOrgFromUser(Email email) {
        ManagerOrgEntity entity = new ManagerOrgEntity();
        entity.id = entity.generateId();
        entity.setCreatedDate(Instant.now());
        entity.setEmail(email);
        entity.registerEvent(OrgCreatedEvent.from(entity.getId()));
        return entity;
    }

    @Override
    protected ManagerOrgId generateId() {
        return ManagerOrgId.newId();
    }

}
