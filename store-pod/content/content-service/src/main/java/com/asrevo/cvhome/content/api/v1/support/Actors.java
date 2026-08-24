package com.asrevo.cvhome.content.api.v1.support;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * The display name of whoever is calling, for {@code createdBy}/{@code updatedBy} and the status audit. Read from
 * the JWT's name-ish claims in order, falling back to the subject.
 */
public final class Actors {

    private static final List<String> NAME_CLAIMS = List.of("name", "full_name", "preferred_username", "email");

    private Actors() {
    }

    public static String current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token && token.getPrincipal() instanceof Jwt jwt) {
            for (String claim : NAME_CLAIMS) {
                Object v = jwt.getClaim(claim);
                if (v instanceof String s && !s.isBlank()) {
                    return s;
                }
            }
            return jwt.getSubject();
        }
        return auth != null ? auth.getName() : "system";
    }

}
