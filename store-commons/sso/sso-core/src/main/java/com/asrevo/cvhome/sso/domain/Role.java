package com.asrevo.cvhome.sso.domain;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

import com.asrevo.cvhome.commons.domain.Permission;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A named set of permissions.
 *
 * <p>
 * The name is what every service authorises on and what the token's {@code roles} claim carries, so for a system
 * role it is fixed. The permissions are data: the effective set of a role is its own plus everything its parent
 * chain grants, and that is what the {@code permissions} claim carries.
 * </p>
 */
@Entity
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(name = "uk_roles_realm_name", columnNames = {"realm_id", "name"})})
@Data
@NoArgsConstructor
public class Role {

    /** How deep an inheritance chain may go before it is treated as a cycle. */
    static final int MAX_DEPTH = 16;

    @Id
    private UUID id;

    /**
     * The realm this row belongs to. Hibernate fills it on insert and adds it to every query; no repository
     * method mentions it. uaa writes one constant value here forever, cua one per store.
     */
    @TenantId
    @Column(name = "realm_id", nullable = false, length = 64)
    private String realmId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RoleScope scope = RoleScope.REALM;

    @Column(name = "system_role", nullable = false)
    private boolean systemRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inherits_from_id")
    private Role inheritsFrom;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", nullable = false, length = 80)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Set<String> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Role(String name) {
        this.name = name;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    /** Own permissions plus the parent chain's, as catalogue values; unknown keys are dropped. */
    public Set<Permission> effectivePermissions() {
        Set<Permission> effective = EnumSet.noneOf(Permission.class);
        Role current = this;
        int depth = 0;
        while (current != null && depth++ < MAX_DEPTH) {
            current.permissions.stream().map(Permission::fromKey).flatMap(java.util.Optional::stream).forEach(effective::add);
            current = current.inheritsFrom;
        }
        return effective;
    }

    /** Whether {@code candidate} is this role or somewhere up its parent chain — the test a new parent must fail. */
    public boolean isOrInheritsFrom(Role candidate) {
        Role current = this;
        int depth = 0;
        while (current != null && depth++ < MAX_DEPTH) {
            if (current.id != null && current.id.equals(candidate.id)) {
                return true;
            }
            current = current.inheritsFrom;
        }
        return false;
    }

}
