package com.asrevo.cvhome.sso.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.repo.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * The account behind a principal name, and the name to show for it.
 *
 * <p>
 * {@code JpaUserDetailsService} makes the principal name the account id, because a username is unique only within
 * a realm. Everything that used to read {@code authentication.getName()} as a username goes through here instead:
 * lockout counters are keyed by username, audit rows are read by people, and both would otherwise have been
 * handed a UUID — silently, in the lockout's case, since a lookup that finds nobody clears nothing.
 * </p>
 *
 * <p>
 * Not every principal name is an account id. A {@code client_credentials} principal is a client id, and a failed
 * login's principal is whatever was typed into the form. Both are returned unchanged rather than treated as an
 * error: the caller wants a label, and the label it already has is the honest one.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class PrincipalNames {

    private final UserRepository users;

    /** The account a principal name denotes; empty when the name is not an account id of this realm. */
    @Transactional(readOnly = true)
    public Optional<User> account(String principalName) {
        if (principalName == null) {
            return Optional.empty();
        }
        try {
            return users.findById(UUID.fromString(principalName));
        } catch (IllegalArgumentException notAnAccountId) {
            return Optional.empty();
        }
    }

    /** The username to show for a principal name, falling back to the name itself. */
    @Transactional(readOnly = true)
    public String display(String principalName) {
        return account(principalName).map(User::getUsername).orElse(principalName);
    }

}
