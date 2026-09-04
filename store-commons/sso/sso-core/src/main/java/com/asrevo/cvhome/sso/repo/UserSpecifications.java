package com.asrevo.cvhome.sso.repo;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.domain.UserStatus;

/**
 * The account list's filters, as JPA specifications.
 *
 * <p>
 * {@link #hasStatus} restates {@link User#status(Instant)} in SQL. The two must agree — the unit test in
 * {@code UserStatusTest} covers the Java side, {@code UserSearchIntegrationTest} the SQL side — and the derivation
 * lives in exactly those two places.
 * </p>
 */
public final class UserSpecifications {

    private static final String ENABLED = "enabled";

    private static final String LOCKED_PERMANENTLY = "lockedPermanently";

    private static final String LOCKED_UNTIL = "lockedUntil";

    private static final String ACTIVATED_AT = "activatedAt";

    private static final String PASSWORD_HASH = "passwordHash";

    private UserSpecifications() {
    }

    public static Specification<User> hasMetadataField(String key, String value) {
        return (root, _, cb) -> cb
                .equal(cb.function("jsonb_extract_path_text", String.class, root.get("metadata"), cb.literal(key)), value);
    }

    /** A case-insensitive contains over username, email and the full name ("Store2 Moderator" matches as typed). */
    public static Specification<User> matches(String q) {
        String pattern = String.format("%%%s%%", q.trim().toLowerCase(Locale.ROOT));
        return (root, _, cb) -> {
            Expression<String> fullName = cb.concat(cb.concat(cb.coalesce(root.get("firstName"), ""), " "),
                    cb.coalesce(root.get("lastName"), ""));
            return cb.or(
                    cb.like(cb.lower(root.get("username")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(fullName), pattern));
        };
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, cb) -> {
            query.distinct(true);
            return cb.equal(root.join("roles").get("name"), roleName);
        };
    }

    public static Specification<User> hasStatus(UserStatus status, Instant now) {
        return (root, _, cb) -> switch (status) {
            case DISABLED -> cb.isFalse(root.get(ENABLED));
            case LOCKED -> cb.and(cb.isTrue(root.get(ENABLED)), locked(root, cb, now));
            case PENDING -> cb.and(cb.isTrue(root.get(ENABLED)), cb.not(locked(root, cb, now)), pending(root, cb));
            case ACTIVE -> cb.and(cb.isTrue(root.get(ENABLED)), cb.not(locked(root, cb, now)), cb.not(pending(root, cb)));
        };
    }

    /** Null-safe: a null {@code locked_until} inside {@code NOT (...)} would drop the row, not keep it. */
    private static Predicate locked(Root<User> root, CriteriaBuilder cb, Instant now) {
        return cb.or(cb.isTrue(root.get(LOCKED_PERMANENTLY)),
                cb.and(cb.isNotNull(root.get(LOCKED_UNTIL)), cb.greaterThan(root.get(LOCKED_UNTIL), now)));
    }

    private static Predicate pending(Root<User> root, CriteriaBuilder cb) {
        return cb.and(cb.isNull(root.get(ACTIVATED_AT)), cb.isNull(root.get(PASSWORD_HASH)));
    }

}
