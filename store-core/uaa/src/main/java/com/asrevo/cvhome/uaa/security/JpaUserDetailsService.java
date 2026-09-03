package com.asrevo.cvhome.uaa.security;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.User;
import com.asrevo.cvhome.uaa.password.PasswordService;
import com.asrevo.cvhome.uaa.repo.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * The account as Spring Security sees it.
 *
 * <p>
 * The flags are what enforce the realm's policy: {@code accountNonLocked} carries the lockout,
 * {@code credentialsNonExpired} the password age, {@code enabled} the administrator's switch. Spring checks all
 * three <em>before</em> the password, so a locked or disabled account fails the same way whether or not the guess
 * was right.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    /**
     * What an account without a password presents to the encoder. A bcrypt hash of nothing anyone knows: it costs the
     * same comparison as a real one and never matches, so an account created without a password fails to sign in
     * exactly like one with a wrong password rather than throwing a 500 out of {@code User.builder().password(null)}.
     */
    static final String NO_PASSWORD = "{bcrypt}$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5XG1Y8V0S0uJ7x1KSyZ0Z6b9Uk3Pm";

    private final UserRepository users;

    private final PasswordService passwords;

    private final Clock clock;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User not found: %s", username)));

        Set<GrantedAuthority> authorities = u.getRoles()
                .stream()
                .map(Role::getName)
                .map(r -> new SimpleGrantedAuthority(String.format("ROLE_%s", r)))
                .collect(Collectors.toSet());
        // The effective permissions as PERM_<key>, so the console can hide what a session cannot do. Nothing
        // authorises on them yet; the roles above remain the gate.
        u.getRoles().stream().flatMap(r -> r.effectivePermissions().stream())
                .map(p -> new SimpleGrantedAuthority(String.format("PERM_%s", p.key())))
                .forEach(authorities::add);

        return org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
                .password(u.getPasswordHash() == null ? NO_PASSWORD : u.getPasswordHash())
                .authorities(authorities)
                .disabled(!u.isEnabled())
                .accountLocked(u.isLocked(clock.instant()))
                .credentialsExpired(passwords.expired(u))
                .build();
    }

}
