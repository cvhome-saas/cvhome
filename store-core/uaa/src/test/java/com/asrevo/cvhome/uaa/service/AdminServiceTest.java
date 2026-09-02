package com.asrevo.cvhome.uaa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.UaaConstants;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.repo.RoleRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The guards in {@link AdminService}: the super admin is untouchable, roles are granted by exact name or not at all.
 */
class AdminServiceTest {

    private static final String ORG = "org";

    private static final String ORG_ID = "org-1";

    private static final String STORE = "store";

    private static final String STORE_ADMIN = "STORE_ADMIN";

    private static final String MISSPELLED = "STORE_ADMN";

    private static final String TEAM = "team";

    private static final String NORTH = "north";

    private static final String NEW_USERNAME = "new";

    private static final String NEW_EMAIL = "new@example.com";

    private static final String NEW_PASSWORD = "Secret-1";

    private static final String HASH_PREFIX = "{hash}";

    private static final String CONCAT = "%s%s";

    private static final String ORG_ADMIN = "ORG_ADMIN";

    private static final String USER = "USER";

    private final UserRepository users = mock(UserRepository.class);

    private final RoleRepository roles = mock(RoleRepository.class);

    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    private final AdminService service = new AdminService(users, roles, encoder);

    private User superAdmin;

    private User ordinary;

    @BeforeEach
    void setUp() {
        superAdmin = new User();
        superAdmin.setId(UaaConstants.SUPER_ADMIN_ID);
        superAdmin.setUsername("super-admin");
        superAdmin.setEmail("owner@example.com");
        ordinary = new User();
        ordinary.setId(UUID.randomUUID());
        ordinary.setUsername("someone");
        ordinary.getMetadata().putAll(new HashMap<>(Map.of(ORG, ORG_ID, STORE, "store-1")));
        Map<UUID, User> store = new HashMap<>(Map.of(superAdmin.getId(), superAdmin, ordinary.getId(), ordinary));
        when(users.findById(any(UUID.class))).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            // The mock stands in for JPA's @PrePersist, which is what assigns the id.
            saved.prePersist();
            store.put(saved.getId(), saved);
            return saved;
        });
        when(encoder.encode(anyString()))
                .thenAnswer(invocation -> String.format(CONCAT, HASH_PREFIX, invocation.getArgument(0)));
    }

    @Test
    void resetPasswordRefusesTheSuperAdminEvenWhenTheirEmailChanged() {
        assertThatThrownBy(() -> service.resetPassword(superAdmin.getId(), new ResetUserPasswordRequest(NEW_PASSWORD)))
                .isInstanceOf(SuperAdminImmutableException.class);
        verify(users, never()).save(any());
    }

    @Test
    void resetPasswordEncodesForAnyoneElse() throws Exception {
        service.resetPassword(ordinary.getId(), new ResetUserPasswordRequest(NEW_PASSWORD));

        assertThat(ordinary.getPasswordHash()).isEqualTo(String.format(CONCAT, HASH_PREFIX, NEW_PASSWORD));
    }

    @Test
    void unknownRoleIsRefusedNotSkipped() {
        when(roles.findByName(MISSPELLED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRoles(ordinary.getId(), Set.of(MISSPELLED)))
                .isInstanceOf(RoleNotFoundException.class);
        assertThat(ordinary.getRoles()).isEmpty();
    }

    @Test
    void superAdminRoleIsNeverGranted() {
        assertThatThrownBy(() -> service.assignRoles(ordinary.getId(), Set.of(UaaConstants.SUPER_ADMIN_ROLE)))
                .isInstanceOf(RoleNotAssignableException.class);
    }

    @Test
    void knownRolesAreGranted() throws Exception {
        when(roles.findByName(STORE_ADMIN)).thenReturn(Optional.of(new Role(STORE_ADMIN)));

        service.assignRoles(ordinary.getId(), Set.of(STORE_ADMIN));

        assertThat(ordinary.getRoles()).extracting(Role::getName).containsExactly(STORE_ADMIN);
    }

    @Test
    void aNullMetadataValueRemovesTheKey() throws Exception {
        Map<String, Object> patch = new HashMap<>();
        patch.put(STORE, null);
        patch.put(TEAM, NORTH);

        service.updateUser(ordinary.getId(), new UpdateUserRequest(null, null, null, null, patch));

        assertThat(ordinary.getMetadata()).containsEntry(ORG, ORG_ID).containsEntry(TEAM, NORTH).doesNotContainKey(STORE);
    }

    @Test
    void createWithoutAPasswordLeavesNoHash() throws Exception {
        service.createUser(new CreateUserRequest(NEW_USERNAME, NEW_EMAIL, null, null, null, Set.of(), Map.of()));

        verify(users).save(any(User.class));
        verify(encoder, never()).encode(anyString());
    }

    @Test
    void createWithAPasswordEncodesIt() throws Exception {
        service.createUser(new CreateUserRequest(NEW_USERNAME, NEW_EMAIL, null, null, NEW_PASSWORD, Set.of(), Map.of()));

        verify(encoder).encode(NEW_PASSWORD);
    }

    @Test
    void assignableRolesExcludeSuperAdminOnly() {
        when(roles.findAll()).thenReturn(List.of(new Role(UaaConstants.SUPER_ADMIN_ROLE), new Role(ORG_ADMIN),
                new Role(USER)));

        assertThat(service.getAssignableRoles()).containsExactlyInAnyOrder(ORG_ADMIN, USER);
    }

}
