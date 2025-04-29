package com.asrevo.cvhome.s2s.jwt;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

@Slf4j
public final class KeyClockJwtGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String DEFAULT_SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private static final String DEFAULT_ROLE_AUTHORITY_PREFIX = "ROLE_";

    private static final String DEFAULT_AUTHORITIES_CLAIM_DELIMITER = " ";

    public static List<String> getRolesAuthorities(Map<String, List<String>> realmAccess) {
        if (realmAccess != null) {
            List<String> roles = realmAccess.get("roles");
            if (roles != null) {
                return castAuthoritiesToCollection(roles).stream()
                        .map(
                                it -> {
                                    String role = it;
                                    if (!it.startsWith(DEFAULT_ROLE_AUTHORITY_PREFIX)) {
                                        role = DEFAULT_ROLE_AUTHORITY_PREFIX + it;
                                    }
                                    return role.toUpperCase();
                                })
                        .filter(it -> it.startsWith(DEFAULT_ROLE_AUTHORITY_PREFIX))
                        .toList();
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public static Collection<String> castAuthoritiesToCollection(Object authorities) {
        return (Collection<String>) authorities;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : getScopeAuthorities(jwt)) {
            grantedAuthorities.add(
                    new SimpleGrantedAuthority(
                            DEFAULT_SCOPE_AUTHORITY_PREFIX + authority.toUpperCase()));
        }
        for (String authority : getRolesAuthorities(jwt.getClaim("realm_access"))) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority));
        }
        return grantedAuthorities;
    }

    private Collection<String> getScopeAuthorities(Jwt jwt) {
        Object authorities = jwt.getClaim("scope");
        if (authorities instanceof String) {
            if (StringUtils.hasText((String) authorities)) {
                return Arrays.asList(
                        ((String) authorities).split(DEFAULT_AUTHORITIES_CLAIM_DELIMITER));
            }
            return Collections.emptyList();
        }
        if (authorities instanceof Collection) {
            return castAuthoritiesToCollection(authorities);
        }
        return Collections.emptyList();
    }
}
