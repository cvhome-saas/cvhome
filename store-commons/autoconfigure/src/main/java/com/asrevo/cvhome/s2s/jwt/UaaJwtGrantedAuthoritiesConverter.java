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
    public static Collection<String> castAuthoritiesToCollection(Collection<?> authorities) {
        return (Collection<String>) authorities;
    }

    public static @NonNull Set<GrantedAuthority> getGrantedAuthorities(Map<String, Object> claims) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        for (String authority : getClaimAsList(claims.get("scope"))) {
            addBothCases(grantedAuthorities, DEFAULT_SCOPE_AUTHORITY_PREFIX, authority);
        }
        for (String authority : getClaimAsList(claims.get("roles"))) {
            addBothCases(grantedAuthorities, DEFAULT_ROLE_AUTHORITY_PREFIX, authority);
        }
        return grantedAuthorities;
    }

    /**
     * Grants both the exact-case authority ({@code SCOPE_super_admin}, what Spring's default converter produces and
     * what uaa's own checks expect) and the uppercased one ({@code SCOPE_SUPER_ADMIN}, what services checking
     * {@code SCOPE_STORE_CORE} against the {@code store_core} scope rely on). Emitting only the uppercase form is
     * what silently locked the admin SDK out of uaa: this converter must never remove an authority the default
     * mapping would have granted.
     */
    private static void addBothCases(Set<GrantedAuthority> target, String defaultPrefix, String authority) {
        String prefix = authority.toUpperCase().startsWith(defaultPrefix) ? "" : defaultPrefix;
        target.add(new SimpleGrantedAuthority(prefix + authority));
        target.add(new SimpleGrantedAuthority(prefix + authority.toUpperCase()));
    }

    private static Collection<String> getClaimAsList(Object claim) {
        if (claim instanceof String sc) {
            if (StringUtils.hasText(sc)) {
                return Arrays.asList(sc.split(DEFAULT_AUTHORITIES_CLAIM_DELIMITER));
            }
            return Collections.emptyList();
        }
        if (claim instanceof Collection<?> c) {
            return castAuthoritiesToCollection(c);
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return getGrantedAuthorities(jwt.getClaims());
    }

}
