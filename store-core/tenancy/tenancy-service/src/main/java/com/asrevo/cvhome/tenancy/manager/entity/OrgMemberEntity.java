package com.asrevo.cvhome.tenancy.manager.entity;

import java.time.Instant;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

import lombok.Getter;
import lombok.Setter;

/**
 * Someone who belongs to an organization.
 *
 * <p>
 * No {@code @Id}: the key is the {@code (org_id, user_id)} pair, and Spring Data JDBC cannot express a composite
 * id on an aggregate root. Rows are written with an explicit insert and read with derived queries, which is all
 * this table needs — it is a join table with two attributes, not an aggregate with behaviour.
 * </p>
 */
@Getter
@Setter
@Table(schema = "tenancy", name = "org_member")
public class OrgMemberEntity {

    @Column("org_id")
    private ManagerOrgId orgId;

    @Column("user_id")
    private String userId;

    @Column("role")
    private String role;

    @Column("added_at")
    private Instant addedAt;

    @Column("added_by")
    private String addedBy;

}
