package com.asrevo.cvhome.sso.service;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditRecord;
import com.asrevo.cvhome.sso.audit.AuditService;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.UaaConstants;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserStatus;
import com.asrevo.cvhome.sso.dto.CreateUserRequest;
import com.asrevo.cvhome.sso.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.sso.dto.UpdateUserRequest;
import com.asrevo.cvhome.sso.dto.UserCounts;
import com.asrevo.cvhome.sso.dto.UserDto;
import com.asrevo.cvhome.sso.dto.UserSearch;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.RoleRepository;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.sso.repo.UserSpecifications;
import com.asrevo.cvhome.sso.security.LockoutService;
import com.asrevo.cvhome.sso.session.SessionAdminService;
import com.asrevo.cvhome.sso.session.SessionSummary;
import com.asrevo.cvhome.sso.token.TokenRevocationService;
import com.asrevo.cvhome.uaa.errors.EmailTakenException;
import com.asrevo.cvhome.uaa.errors.PasswordCompromisedException;
import com.asrevo.cvhome.uaa.errors.PasswordPolicyViolationException;
import com.asrevo.cvhome.uaa.errors.PasswordReusedException;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SessionNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.errors.UsernameTakenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toSet;

/**
 * Platform-wide user administration.
 *
 * <p>
 * Disabling, deleting or resetting an account also ends its sessions and tokens — the token store is real now, so
 * "disabled" means "signed out everywhere", not "signed out at the next login".
 * </p>
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

    static final String EMAIL = "email";

    private static final String COMMA = ",";

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordService passwords;

    private final AuditService audit;

    private final Clock clock;

    private final SessionAdminService sessions;

    private final TokenRevocationService tokens;

    private final LockoutService lockout;

    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(UserSearch search, Pageable pageable) {
        return userRepository.findAll(buildSpec(search, clock.instant()), pageable).map(this::toDto);
    }

    /** The Users screen's tiles: one count per status, derived the same way the list's status filter is. */
    @Transactional(readOnly = true)
    public UserCounts counts() {
        Instant now = clock.instant();
        return new UserCounts(userRepository.count(),
                countByStatus(UserStatus.ACTIVE, now), countByStatus(UserStatus.PENDING, now),
                countByStatus(UserStatus.LOCKED, now), countByStatus(UserStatus.DISABLED, now));
    }

    private long countByStatus(UserStatus status, Instant now) {
        return userRepository.count(UserSpecifications.hasStatus(status, now));
    }

    private static Specification<User> buildSpec(UserSearch search, Instant now) {
        Specification<User> spec = (_, _, cb) -> cb.conjunction();
        if (search == null) {
            return spec;
        }
        if (search.hasQuery()) {
            spec = spec.and(UserSpecifications.matches(search.q()));
        }
        if (search.status() != null) {
            spec = spec.and(UserSpecifications.hasStatus(search.status(), now));
        }
        if (search.hasRole()) {
            spec = spec.and(UserSpecifications.hasRole(search.role()));
        }
        if (search.metadata() != null) {
            for (Map.Entry<String, String> entry : search.metadata().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    spec = spec.and(UserSpecifications.hasMetadataField(entry.getKey(), entry.getValue()));
                }
            }
        }
        return spec;
    }

    public UserDto toDto(User u) {
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
     * that never matches rather than as an error. A taken username or email is a 409 naming the field, not the
     * database's unique violation.
     */
    @Transactional
    public UserDto createUser(CreateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException,
            PasswordPolicyViolationException, PasswordReusedException, PasswordCompromisedException, UsernameTakenException,
            EmailTakenException {
        User saved = newAccount(req);
        if (req.password() != null && !req.password().isBlank()) {
            passwords.setPassword(saved, req.password());
        }
        return finishCreate(saved, req.roles());
    }

    /** {@link #createUser} without a password: the shape an invitation starts from. Ignores {@code req.password()}. */
    @Transactional
    public UserDto createAccount(CreateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException,
            UsernameTakenException, EmailTakenException {
        return finishCreate(newAccount(req), req.roles());
    }

    private User newAccount(CreateUserRequest req) throws UsernameTakenException, EmailTakenException {
        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw UsernameTakenException.of(req.username());
        }
        if (req.email() != null && userRepository.existsByEmailIgnoreCase(req.email())) {
            throw EmailTakenException.of(req.email());
        }
        User u = User.create(req.username(), req.email(), req.firstName(), req.lastName());
        if (req.metadata() != null) {
            applyMetadata(u, req.metadata());
        }
        return userRepository.save(u);
    }

    private UserDto finishCreate(User saved, Set<String> roles)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        if (roles != null && !roles.isEmpty()) {
            assignRoles(saved.getId(), roles);
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
     * {@code null} value is <em>removed</em> — the only way to unset {@code org} or {@code store} once written. A new
     * email is unverified until a link reaches it.
     */
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException,
            EmailTakenException {
        User u = getNonSuperAdmin(id);
        UserDto before = toDto(u);
        if (req.firstName() != null) {
            u.setFirstName(req.firstName());
        }
        if (req.lastName() != null) {
            u.setLastName(req.lastName());
        }
        if (req.email() != null && !req.email().equalsIgnoreCase(u.getEmail())) {
            changeEmail(u, req.email());
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

    private void changeEmail(User u, String email) throws EmailTakenException {
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, u.getId())) {
            throw EmailTakenException.of(email);
        }
        String previous = u.getEmail();
        u.setEmail(email);
        u.setEmailVerified(false);
        audit.record(event(AuditEventType.USER_EMAIL_CHANGED, u).change(Map.of(EMAIL, previous), Map.of(EMAIL, email)));
    }

    /** Marks the address verified on an administrator's word — say, after they confirmed it out of band. */
    @Transactional
    public UserDto verifyEmail(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        if (!u.isEmailVerified()) {
            u.setEmailVerified(true);
            audit.record(event(AuditEventType.USER_EMAIL_VERIFIED, u));
        }
        return toDto(u);
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

    /**
     * Sets a password through the policy, then ends every session and token the account holds: a reset is what an
     * administrator does when the old password may be in the wrong hands. Refuses the super admin, whose password
     * comes from configuration alone.
     */
    @Transactional
    public void resetPassword(UUID userId, ResetUserPasswordRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, PasswordPolicyViolationException,
            PasswordReusedException, PasswordCompromisedException {
        User u = getNonSuperAdmin(userId);
        passwords.setPassword(u, req.password());
        userRepository.save(u);
        revokeEverything(u);
        audit.record(event(AuditEventType.USER_PASSWORD_RESET, u).reason(ADMIN_RESET));
    }

    /** Clears a lockout. Idempotent: unlocking an unlocked account is not an error. */
    @Transactional
    public void unlock(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        lockout.unlock(getNonSuperAdmin(id));
    }

    private void revokeEverything(User u) {
        sessions.revokeAll(u, null);
        tokens.revokeAllForUser(u);
    }

    @Transactional(readOnly = true)
    public List<SessionSummary> listSessions(UUID id) throws UserNotFoundException {
        return sessions.list(findUser(id), null);
    }

    @Transactional
    public void revokeSession(UUID id, String sessionId) throws UserNotFoundException, SessionNotFoundException {
        sessions.revoke(findUser(id), sessionId);
    }

    /** Signs the account out everywhere. */
    @Transactional
    public int revokeSessions(UUID id) throws UserNotFoundException {
        return sessions.revokeAll(findUser(id), null);
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

    /** Switches the account off and signs it out everywhere; the aggregate publishes {@code UserDisabledEvent}. */
    @Transactional
    public void disableUser(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        u.disable();
        userRepository.save(u);
        revokeEverything(u);
        audit.record(event(AuditEventType.USER_DISABLED, u));
    }

    /** Deleted through the aggregate, not by id, so {@code UserDeletedEvent} reaches the outbox with the delete. */
    @Transactional
    public void delete(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        User u = getNonSuperAdmin(id);
        UserDto before = toDto(u);
        revokeEverything(u);
        u.markDeleted();
        userRepository.delete(u);
        audit.record(event(AuditEventType.USER_DELETED, u).change(before, null));
    }

    /** The account, unless it is the seeded super admin, which no mutator may touch. */
    public User getNonSuperAdmin(UUID id) throws UserNotFoundException, SuperAdminImmutableException {
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
