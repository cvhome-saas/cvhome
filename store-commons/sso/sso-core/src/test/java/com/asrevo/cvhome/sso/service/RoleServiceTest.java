package com.asrevo.cvhome.sso.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.RoleScope;
import com.asrevo.cvhome.sso.dto.CreateRoleRequest;
import com.asrevo.cvhome.sso.dto.RoleDto;
import com.asrevo.cvhome.sso.dto.UpdateRoleRequest;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.uaa.errors.DuplicateRoleNameException;
import com.asrevo.cvhome.uaa.errors.PermissionUnknownException;
import com.asrevo.cvhome.uaa.errors.RoleInUseException;
import com.asrevo.cvhome.uaa.errors.RoleInheritanceCycleException;
import com.asrevo.cvhome.uaa.errors.RoleNameInvalidException;
import com.asrevo.cvhome.uaa.errors.SystemRoleImmutableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * System roles keep their names, inheritance cannot loop, permissions come from the catalogue.
 */
class RoleServiceTest {

    private static final String USERS_READ = "users:read";

    private static final String USERS_WRITE = "users:write";

    private static final String STAFF = "STAFF";

    private static final String OWNER = "OWNER";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String OWNS = "Owns an org";

    private final RoleRepository roles = mock(RoleRepository.class);

    private final RoleService service = new RoleService(roles, mock(AuditService.class));

    private Role role(String name, boolean system, String... permissions) {
        Role role = new Role(name);
        role.prePersist();
        role.setSystemRole(system);
        role.setPermissions(new HashSet<>(Set.of(permissions)));
        when(roles.findById(role.getId())).thenReturn(Optional.of(role));
        when(roles.save(role)).thenReturn(role);
        return role;
    }

    @Test
    void effectivePermissionsFollowTheParentChain() {
        Role staff = role(STAFF, false, USERS_READ);
        Role owner = role(OWNER, false, USERS_WRITE);
        owner.setInheritsFrom(staff);

        RoleDto dto = service.toDto(owner);

        assertThat(dto.permissions()).containsExactly(USERS_WRITE);
        assertThat(dto.effectivePermissions()).containsExactly(USERS_READ, USERS_WRITE);
    }

    @Test
    void aSystemRoleCannotBeRenamedOrDeleted() {
        Role admin = role(ORG_ADMIN, true, USERS_READ);

        assertThatThrownBy(() -> service.update(admin.getId(), new UpdateRoleRequest(OWNER, null, null, null, null, null)))
                .isInstanceOf(SystemRoleImmutableException.class);
        assertThatThrownBy(() -> service.delete(admin.getId())).isInstanceOf(SystemRoleImmutableException.class);
    }

    @Test
    void aSystemRolesPermissionsStayEditable() throws Exception {
        Role admin = role(ORG_ADMIN, true, USERS_READ);

        RoleDto updated = service.update(admin.getId(),
                new UpdateRoleRequest(null, OWNS, null, null, null, Set.of(USERS_READ, USERS_WRITE)));

        assertThat(updated.permissions()).containsExactly(USERS_READ, USERS_WRITE);
        assertThat(updated.description()).isEqualTo(OWNS);
    }

    @Test
    void inheritanceCyclesAreRefused() {
        Role a = role("A_ROLE", false);
        Role b = role("B_ROLE", false);
        b.setInheritsFrom(a);

        assertThatThrownBy(() -> service.update(a.getId(), new UpdateRoleRequest(null, null, null, b.getId(), null, null)))
                .isInstanceOf(RoleInheritanceCycleException.class);
        assertThatThrownBy(() -> service.update(a.getId(), new UpdateRoleRequest(null, null, null, a.getId(), null, null)))
                .isInstanceOf(RoleInheritanceCycleException.class);
    }

    @Test
    void unknownPermissionsAreRefused() {
        when(roles.findByName(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new CreateRoleRequest(STAFF, null, RoleScope.REALM, null, Set.of("users:fly"))))
                .isInstanceOf(PermissionUnknownException.class);
    }

    @Test
    void namesAreNormalisedAndValidated() throws Exception {
        when(roles.findByName(anyString())).thenReturn(Optional.empty());
        when(roles.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            saved.prePersist();
            return saved;
        });

        RoleDto created = service.create(new CreateRoleRequest(" staff ", null, null, null, Set.of(USERS_READ)));

        assertThat(created.name()).isEqualTo(STAFF);
        assertThat(created.scope()).isEqualTo(RoleScope.REALM);
        assertThatThrownBy(() -> service.create(new CreateRoleRequest("store-admin", null, null, null, Set.of())))
                .isInstanceOf(RoleNameInvalidException.class);
    }

    @Test
    void duplicateNamesAreRefused() {
        when(roles.findByName(STAFF)).thenReturn(Optional.of(new Role(STAFF)));

        assertThatThrownBy(() -> service.create(new CreateRoleRequest(STAFF, null, null, null, Set.of())))
                .isInstanceOf(DuplicateRoleNameException.class);
    }

    @Test
    void aHeldRoleCannotBeDeleted() {
        Role custom = role(STAFF, false);
        when(roles.countHolders(custom.getId())).thenReturn(3L);

        assertThatThrownBy(() -> service.delete(custom.getId())).isInstanceOf(RoleInUseException.class);
    }

    @Test
    void theCatalogueIsTheEnum() {
        assertThat(RoleService.catalogue()).extracting("key").contains(USERS_READ, "settings:write", "audit:read");
        assertThat(service.toDto(role(STAFF, false)).userCount()).isZero();
        assertThat(UUID.randomUUID()).isNotNull();
    }

}
