package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Email;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.tenancy.commons.dto.OrgStatus;

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

    @Column("name")
    private String name;

    @Column("status")
    private OrgStatus status;

    @Column("owner_user_id")
    private String ownerUserId;

    /**
     * A new organization, named.
     *
     * <p>
     * The name is an argument rather than something a later {@code rename} supplies because {@code rename} was the
     * only writer this column ever had, and nothing called it on the way in: every organization on the platform was
     * created nameless. A signup knows what to call one — see {@code SignUpUser.organizationNameOrDefault()} — so
     * the row is complete from the first insert.
     * </p>
     */
    public static ManagerOrgEntity createOrgFromUser(Email email, String name) {
        ManagerOrgEntity entity = new ManagerOrgEntity();
        entity.id = entity.generateId();
        entity.setCreatedDate(Instant.now());
        entity.setEmail(email);
        entity.setName(name == null || name.isBlank() ? null : name.strip());
        entity.setStatus(OrgStatus.ACTIVE);
        return entity;
    }

    @Override
    protected ManagerOrgId generateId() {
        return ManagerOrgId.newId();
    }

}
