package com.asrevo.cvhome.sso.security;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.sso.domain.Role;
import com.asrevo.cvhome.sso.domain.User;
import com.asrevo.cvhome.sso.dto.MeResponse;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;

import lombok.RequiredArgsConstructor;

/**
 * Turns whatever authenticated the request into the account behind it.
 *
 * <p>
 * Two shapes reach uaa: a form-login session, whose principal name is the username, and a bearer JWT, whose
 * {@code sub} is the username for user tokens and the client id for {@code client_credentials} tokens. The second
 * kind has no account, which is a {@link NotAUserPrincipalException} — a 403, because the token itself is fine.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    static final String VIA_SESSION = "SESSION";

    static final String VIA_JWT = "JWT";

    private final UserRepository users;

    @Transactional(readOnly = true)
    public MeResponse describe(Authentication authentication) throws NotAUserPrincipalException {
        User user = resolve(authentication);
        List<MeResponse.AuthorityDto> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .map(MeResponse.AuthorityDto::new)
                .toList();
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toCollection(TreeSet::new));
        Set<String> permissions = user.getRoles().stream().flatMap(r -> r.effectivePermissions().stream())
                .map(Permission::key).collect(Collectors.toCollection(TreeSet::new));
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                roles, permissions, authorities, authentication instanceof JwtAuthenticationToken ? VIA_JWT : VIA_SESSION);
    }

    @Transactional(readOnly = true)
    public User resolve(Authentication authentication) throws NotAUserPrincipalException {
        String name = authentication.getName();
        if (authentication.getPrincipal() instanceof Jwt jwt && jwt.getClaimAsString("uid") == null) {
            // A user token always carries uid; a client-credentials token never does.
            throw NotAUserPrincipalException.of(name);
        }
        return users.findByUsername(name).orElseThrow(() -> NotAUserPrincipalException.of(name));
    }

}
