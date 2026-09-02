package com.asrevo.cvhome.uaa.service;

import java.time.Clock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.UaaConstants;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.repo.RoleRepository;
import com.asrevo.cvhome.uaa.repo.UserRepository;
import com.asrevo.cvhome.uaa.repo.UserSpecifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toSet;

/**
 * Platform-wide user administration.
 *
 * <p>
 * Every mutator except {@link #getUsers} and {@link #usernameExist} refuses the seeded super admin — including
 * {@link #resetPassword}, which used to skip the guard and so let any {@code super_admin} token set that account's
 * password and sign in as it. The guard is keyed on {@link UaaConstants#SUPER_ADMIN_ID}, never on an email.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    static final String ROLES = "roles";

    static final String ADMIN_RESET = "ADMIN_RESET";

    private static final String COMMA = ",";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditService audit;

    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Map<String, String> metadataFilters, Pageable pageable) {
        Specification<User> spec = buildSpec(metadataFilters);
        return userRepository.findAll(spec, pageable).map(this::toDto);
    }

    private Specification<User> buildSpec(Map<String, String> metadataFilters) {
        Specification<User> spec = (_, _, cb) -> cb.conjunction();
        if (metadataFilters != null) {
            for (Map.Entry<String, String> entry : metadataFilters.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    spec = spec.and(UserSpecifications.hasMetadataField(entry.getKey(), entry.getValue()));
                }
            }
        }
        return spec;
    }

    UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(), u.getEmail(), u.getFirstName(), u.getLastName(), u.isEnabled(),
                u.status(clock.instant()), u.isEmailVerified(), u.getRoles().stream().map(Role::getName).collect(toSet()),
                u.getMetadata(), u.getLastSignInAt(), u.getLastSignInClientId(), u.getLastSignInVia(), u.getLockedUntil(),
                u.getFailedLoginAttempts(), u.getPasswordChangedAt(), u.getCreatedAt());
    }

    private static AuditRecord event(AuditEventType type, User u) {
        return AuditRecord.of(type).user(u.getId(), u.getUsername());
    }

    // orElseThrow is generic over the thrown type, so a checked exception needs no Unchecked wrapper here.
    private User findUser(UUID id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> UserNotFoundException.of(id));
    }

    public UserDto getUser(UUID id) throws UserNotFoundException {
        return toDto(findUser(id));
    }

    /**
     * Creates an account. With a password the account can sign in at once; without one it exists, is enabled, and
     * cannot sign in until a password is set — {@code JpaUserDetailsService} treats a missing hash as a credential
     * that never matches rather than as an error.
     */
    @Transactional
    public UserDto createUser(CreateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setFirstName(req.firstName());
        u.setLastName(req.lastName());
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
            u.setPasswordChangedAt(clock.instant());
        }
        if (req.metadata() != null) {
            applyMetadata(u, req.metadata());
        }
        User saved = userRepository.save(u);
        if (req.roles() != null && !req.roles().isEmpty()) {
            assignRoles(saved.getId(), req.roles());
        }
        UserDto created = getUser(saved.getId());
        audit.record(event(AuditEventType.USER_CREATED, saved).change(null, created));
        return created;
    }

    /**
     * Grants roles by their bare name ({@code STORE_ADMIN}, not {@code ROLE_STORE_ADMIN}). An unknown name is a
     * {@link RoleNotFoundException}, and {@code SUPER_ADMIN} a {@link RoleNotAssignableException}: neither is skipped.
     */
    @Transactional
    public void assignRoles(UUID id, Set<String> roleNames)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        User u = getNonSuperAdmin(id);
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }
        Set<String> before = u.getRoles().stream().map(Role::getName).collect(toSet());
        for (String name : roleNames) {
            u.getRoles().add(assignableRole(name));
        }
        audit.record(event(AuditEventType.USER_ROLE_ASSIGNED, u).detail(String.join(COMMA, new TreeSet<>(roleNames)))
                .change(Map.of(ROLES, before), Map.of(ROLES, u.getRoles().stream().map(Role::getName).collect(toSet()))));
    }

    private Role assignableRole(String name) throws RoleNotFoundException, RoleNotAssignableException {
        if (UaaConstants.SUPER_ADMIN_ROLE.equals(name)) {
            throw RoleNotAssignableException.of(name);
        }
        return roleRepository.findByName(name).orElseThrow(() -> RoleNotFoundException.named(name));
    }

    /**
     * Partial update: an absent field is left alone. {@code metadata} merges key by key, and a key sent with a
     * {@code null} value is <em>removed</em> — the only way to unset {@code org} or {@code store} once written.
     */
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        User u = getNonSuperAdmin(id);
        UserDto before = toDto(u);
        if (req.firstName() != null) {
            u.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            u.setLastName(req.lastName());
        }
        if (req.enabled() != null) {
            u.setEnabled(req.enabled());
        }
        if (req.metadata() != null) {
            applyMetadata(u, req.metadata());
        }
        if (req.roles() != null) {
            Set<Role> resolved = new HashSet<>();
            for (String name : req.roles()) {
                resolved.add(assignableRole(name));
            }
            u.getRoles().clear();
            u.getRoles().addAll(resolved);
        }
        User saved = userRepository.save(u);
        UserDto after = getUser(saved.getId());
        audit.record(event(AuditEventType.USER_UPDATED, saved).change(before, after));
        return after;
    }

    private static void applyMetadata(User u, Map<String, Object> metadata) {
        metadata.forEach((key, value) -> {
            if (value == null) {
                u.getMetadata().remove(key);
            } else {
                u.getMetadata().put(key, value);
            }
        });
    }

    @Transactional
    public void removeRoles(UUID id, Set<String> roleNames) throws UserNotFoundException, SuperAdminImmutableException {
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }
        User u = getNonSuperAdmin(id);
        Set<String> before = u.getRoles().stream().map(Role::getName).collect(toSet());
        u.getRoles().removeIf(r -> roleNames.contains(r.getName()));
        audit.record(event(AuditEventType.USER_ROLE_REMOVED, u).detail(String.join(COMMA, new TreeSet<>(roleNames)))
                .change(Map.of(ROLES, before), Map.of(ROLES, u.getRoles().stream().map(Role::getName).collect(toSet()))));
    }

    /** Sets a password. Refuses the super admin: that account's password comes from configuration alone. */
    @Transactional
    public void resetPassword(UUID userId, ResetUserPasswordRequest req)
            throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(userId);
        if (req.password() != null && !req.password().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.password()));
            u.setPasswordChangedAt(clock.instant());
            if (u.getActivatedAt() == null) {
                u.setActivatedAt(clock.instant());
            }
        }
        userRepository.save(u);
        audit.record(event(AuditEventType.USER_PASSWORD_RESET, u).reason(ADMIN_RESET));
    }

    @Transactional(readOnly = true)
    public boolean usernameExist(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public void enableUser(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        u.setEnabled(true);
        audit.record(event(AuditEventType.USER_ENABLED, u));
    }

    @Transactional
    public void disableUser(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        u.setEnabled(false);
        audit.record(event(AuditEventType.USER_DISABLED, u));
    }

    @Transactional
    public void delete(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        UserDto before = toDto(u);
        userRepository.deleteById(u.getId());
        audit.record(event(AuditEventType.USER_DELETED, u).change(before, null));
    }

    private User getNonSuperAdmin(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User user = findUser(id);
        if (UaaConstants.SUPER_ADMIN_ID.equals(user.getId())) {
            throw SuperAdminImmutableException.of(id);
        }
        return user;
    }

    /** Every role except {@code SUPER_ADMIN}. */
    public Set<String> getAssignableRoles() {
        return roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .filter(roleName -> !UaaConstants.SUPER_ADMIN_ROLE.equals(roleName))
                .collect(toSet());
    }

}
