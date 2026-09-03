package com.asrevo.cvhome.sso.security;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.asrevo.cvhome.sso.realm.SsoRealmProperties;
import com.asrevo.cvhome.sso.repo.UserRepository;
import com.asrevo.cvhome.uaa.errors.NotAUserPrincipalException;

import lombok.RequiredArgsConstructor;

/**
 * Turns whatever authenticated the request into the account behind it.
 *
 * <p>
 * Two shapes reach it: a form-login session, whose principal name is the username, and a bearer JWT.
 * </p>
 *
 * <p>
 * A JWT is resolved by its {@code uid} claim, not by its subject. Only a user token carries {@code uid}, so its
 * absence is what identifies a {@code client_credentials} caller — a {@link NotAUserPrincipalException}, a 403
 * because the token itself is fine. And the subject is not a username everywhere: in a multi-realm deployment it
 * is the account id, because a username is unique only within its realm. Looking up by name there found nobody
 * and reported a signed-in shopper as a service client.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolver {

    static final String VIA_SESSION = "SESSION";

    static final String VIA_JWT = "JWT";

    private final UserRepository users;

    private final SsoRealmProperties realmProperties;

    @Transactional(readOnly = true)
    public MeResponse describe(Authentication authentication) throws NotAUserPrincipalException {
        User user = resolve(authentication);
        List<MeResponse.AuthorityDto> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .map(MeResponse.AuthorityDto::new)
                .toList();
        // The deployment's default roles as well as the granted ones, so this answers what the token says. cua
        // gives every shopper CUSTOMER by configuration rather than by a row, and a person asking who they are
        // should not be told something different from what their token claims.
        Set<String> roles = Stream.concat(user.getRoles().stream().map(Role::getName),
                realmProperties.getDefaultRoles().stream()).collect(Collectors.toCollection(TreeSet::new));
        Set<String> permissions = user.getRoles().stream().flatMap(r -> r.effectivePermissions().stream())
                .map(Permission::key).collect(Collectors.toCollection(TreeSet::new));
        return new MeResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(),
                roles, permissions, authorities, authentication instanceof JwtAuthenticationToken ? VIA_JWT : VIA_SESSION);
    }

    @Transactional(readOnly = true)
    public User resolve(Authentication authentication) throws NotAUserPrincipalException {
        String name = authentication.getName();
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            // A user token always carries uid; a client-credentials token never does.
            String uid = jwt.getClaimAsString("uid");
            if (uid == null) {
                throw NotAUserPrincipalException.of(name);
            }
            return users.findById(accountId(uid, name)).orElseThrow(() -> NotAUserPrincipalException.of(name));
        }
        return users.findByUsername(name).orElseThrow(() -> NotAUserPrincipalException.of(name));
    }

    private static UUID accountId(String uid, String name) throws NotAUserPrincipalException {
        try {
            return UUID.fromString(uid);
        } catch (IllegalArgumentException malformed) {
            throw NotAUserPrincipalException.of(name);
        }
    }

}
