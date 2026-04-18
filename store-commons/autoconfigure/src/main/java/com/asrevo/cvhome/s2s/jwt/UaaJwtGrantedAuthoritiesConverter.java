package com.asrevo.cvhome.s2s.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class UaaJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String DEFAULT_SCOPE_AUTHORITY_PREFIX = "SCOPE_";

    private static final String DEFAULT_ROLE_AUTHORITY_PREFIX = "ROLE_";

    private static final String DEFAULT_AUTHORITIES_CLAIM_DELIMITER = " ";

    @SuppressWarnings("unchecked")
    public static Collection<String> castAuthoritiesToCollection(Object authorities) {
        return (Collection<String>) authorities;
    }

    public static @NonNull Set<GrantedAuthority> getGrantedAuthorities(Map<String, Object> claims) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (String authority : getClaimAsList(claims.get("scope"))) {
            String prefix = "";
            if (!authority.toUpperCase().startsWith(DEFAULT_SCOPE_AUTHORITY_PREFIX)) {
                prefix = DEFAULT_SCOPE_AUTHORITY_PREFIX;
            }
            grantedAuthorities.add(new SimpleGrantedAuthority(prefix + authority.toUpperCase()));
        }
        for (String authority : getClaimAsList(claims.get("roles"))) {
            String prefix = "";
            if (!authority.toUpperCase().startsWith(DEFAULT_ROLE_AUTHORITY_PREFIX)) {
                prefix = DEFAULT_ROLE_AUTHORITY_PREFIX;
            }
            grantedAuthorities.add(new SimpleGrantedAuthority(prefix + authority.toUpperCase()));

        }
        return grantedAuthorities;
    }

    private static Collection<String> getClaimAsList(Object claim) {
        if (claim instanceof String) {
            if (StringUtils.hasText((String) claim)) {
                return Arrays.asList(((String) claim).split(DEFAULT_AUTHORITIES_CLAIM_DELIMITER));
            }
            return Collections.emptyList();
        }
        if (claim instanceof Collection) {
            return castAuthoritiesToCollection(claim);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return getGrantedAuthorities(jwt.getClaims());
    }

}
