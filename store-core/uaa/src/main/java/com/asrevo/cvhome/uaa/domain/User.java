package com.asrevo.cvhome.uaa.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.AbstractAggregateRoot;

import com.asrevo.cvhome.uaa.events.InvitationIssuedEvent;
import com.asrevo.cvhome.uaa.events.PasswordResetLinkIssuedEvent;
import com.asrevo.cvhome.uaa.events.UserCreatedEvent;
import com.asrevo.cvhome.uaa.events.UserDeletedEvent;
import com.asrevo.cvhome.uaa.events.UserDisabledEvent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * An account, and the aggregate root uaa's events are registered on.
 *
 * <p>
 * The lifecycle transitions that other services care about — created, disabled, deleted, a link issued — are
 * methods here that record the matching {@code uaa-events} record; Spring Data publishes them when the repository
 * saves (or deletes) the aggregate, in the same transaction, and the outbox takes them from there. A service class
 * never publishes a user event itself.
 * </p>
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@NoArgsConstructor
public class User extends AbstractAggregateRoot<User> {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 190)
    private String username;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    /** When the account first became usable — a password set, an invitation accepted. Null means pending. */
    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;

    @Column(name = "lockout_count", nullable = false)
    private int lockoutCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "locked_permanently", nullable = false)
    private boolean lockedPermanently;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    @Column(name = "last_sign_in_at")
    private Instant lastSignInAt;

    @Column(name = "last_sign_in_client_id", length = 100)
    private String lastSignInClientId;

    @Column(name = "last_sign_in_ip", length = 45)
    private String lastSignInIp;

    /** {@code PASSWORD}, or {@code IDP:<alias>} once brokered logins exist. */
    @Column(name = "last_sign_in_via", length = 60)
    private String lastSignInVia;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata = new HashMap<>();

    /** A new account with its id already assigned, so the created event can name it before the insert. */
    public static User create(String username, String email, String firstName, String lastName) {
        User u = new User();
        u.id = UUID.randomUUID();
        u.username = username;
        u.email = email;
        u.firstName = firstName;
        u.lastName = lastName;
        u.registerEvent(new UserCreatedEvent(u.id.toString(), username, email));
        return u;
    }

    public void disable() {
        this.enabled = false;
        registerEvent(new UserDisabledEvent(id.toString(), username));
    }

    /** Called before the repository delete, which is what publishes the event. */
    public void markDeleted() {
        registerEvent(new UserDeletedEvent(id.toString(), username, email));
    }

    public void invitationIssued(String link, Instant expiresAt, String locale) {
        registerEvent(new InvitationIssuedEvent(id.toString(), username, email, displayName(), link, expiresAt, locale));
    }

    public void resetLinkIssued(String link, Instant expiresAt, String locale) {
        registerEvent(new PasswordResetLinkIssuedEvent(id.toString(), username, email, displayName(), link, expiresAt,
                locale));
    }

    /** The name a message addresses the person by: the given and family names, or the username. */
    public String displayName() {
        String name = String.join(" ", firstName == null ? "" : firstName, lastName == null ? "" : lastName).trim();
        return name.isEmpty() ? username : name;
    }

    /** Whether the account has never been usable: no password and never activated. */
    public boolean isPending() {
        return activatedAt == null && passwordHash == null;
    }

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (activatedAt == null && passwordHash != null) {
            activatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isLocked(Instant now) {
        return lockedPermanently || lockedUntil != null && lockedUntil.isAfter(now);
    }

    public UserStatus status(Instant now) {
        if (!enabled) {
            return UserStatus.DISABLED;
        }
        if (isLocked(now)) {
            return UserStatus.LOCKED;
        }
        if (isPending()) {
            return UserStatus.PENDING;
        }
        return UserStatus.ACTIVE;
    }

}
