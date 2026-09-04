package com.asrevo.cvhome.sso.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.password.PasswordService;
import com.asrevo.cvhome.sso.repo.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The account as Spring Security sees it.
 *
 * <p>
 * <strong>The principal name is the account id, not the typed username.</strong> Spring Session's principal index
 * and {@code oauth2_authorization.principal_name} are both looked up by this value, and a username is unique only
 * within its realm — on cua, where every store is a realm, two shoppers called "user" in two stores shared one
 * principal name, and listing or revoking one account's sessions reached the other store's. That is a tenant leak,
 * so it is pinned here.
 * </p>
 *
 * <p>
 * An account with no password hash presents a bcrypt hash of something nobody knows rather than {@code null}: the
 * encoder does the same work and never matches, so it fails to sign in exactly like a wrong password instead of
 * throwing a 500 out of the builder.
 * </p>
 */
class JpaUserDetailsServiceTest {

    private static final String USERNAME = "someone";
    private static final String HASH = "{bcrypt}$2a$10$abcdefghijklmnopqrstuv";
    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordService passwords = mock(PasswordService.class);
    private final JpaUserDetailsService service =
            new JpaUserDetailsService(users, passwords, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void theprincipalNameIsTheAccountIdRatherThanTheTypedUsername() {
        given(user());

        // A username is unique only within its realm; two stores' "user" would share one principal name.
        assertThat(service.loadUserByUsername(USERNAME).getUsername()).isEqualTo(ACCOUNT_ID.toString());
    }

    @Test
    void rolesBecomeRoleAuthoritiesAndTheirPermissionsBecomePermOnes() {
        User user = user();
        user.setRoles(Set.of(role("ADMIN", Permission.USERS_READ, Permission.USERS_WRITE)));
        given(user);

        assertThat(service.loadUserByUsername(USERNAME).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "PERM_users:read", "PERM_users:write");
    }

    @Test
    void aroleWithNoPermissionsStillGrantsItsOwnRoleAuthority() {
        User user = user();
        user.setRoles(Set.of(role("USER")));
        given(user);

        assertThat(service.loadUserByUsername(USERNAME).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void anAccountWithNoPasswordPresentsAhashThatNeverMatchesRatherThanNull() {
        User user = user();
        user.setPasswordHash(null);
        given(user);

        UserDetails details = service.loadUserByUsername(USERNAME);

        // Same comparison cost, never a match, and no 500 out of the builder.
        assertThat(details.getPassword()).isEqualTo(JpaUserDetailsService.NO_PASSWORD);
    }

    @Test
    void theStoredHashIsPassedThroughUntouchedWhenThereIsOne() {
        given(user());

        assertThat(service.loadUserByUsername(USERNAME).getPassword()).isEqualTo(HASH);
    }

    @Test
    void anActiveAccountIsEnabledUnlockedAndItsCredentialsAreCurrent() {
        given(user());

        UserDetails details = service.loadUserByUsername(USERNAME);

        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void thedisabledSwitchTheLockoutAndThePasswordAgeEachSetTheirOwnFlag() {
        User user = user();
        user.setEnabled(false);
        user.setLockedUntil(NOW.plusSeconds(60));
        given(user);
        when(passwords.expired(user)).thenReturn(true);

        UserDetails details = service.loadUserByUsername(USERNAME);

        // Spring checks all three before the password, so a locked account fails the same way whether or not
        // the guess was right.
        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isCredentialsNonExpired()).isFalse();
    }

    @Test
    void alockoutThatHasAlreadyExpiredNoLongerLocksTheAccount() {
        User user = user();
        user.setLockedUntil(NOW.minusSeconds(1));
        given(user);

        assertThat(service.loadUserByUsername(USERNAME).isAccountNonLocked()).isTrue();
    }

    @Test
    void apermanentLockIsNotWaitedOut() {
        User user = user();
        user.setLockedPermanently(true);
        given(user);

        assertThat(service.loadUserByUsername(USERNAME).isAccountNonLocked()).isFalse();
    }

    @Test
    void anUnknownUsernameIsRefusedRatherThanReturningAnEmptyPrincipal() {
        when(users.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(USERNAME);
    }

    private void given(User user) {
        when(users.findByUsername(USERNAME)).thenReturn(Optional.of(user));
    }

    private static User user() {
        User user = new User();
        user.setId(ACCOUNT_ID);
        user.setUsername(USERNAME);
        user.setEmail("someone@example.com");
        user.setPasswordHash(HASH);
        return user;
    }

    private static Role role(String name, Permission... permissions) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        role.setPermissions(java.util.Arrays.stream(permissions).map(Permission::key)
                .collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
        return role;
    }

}
