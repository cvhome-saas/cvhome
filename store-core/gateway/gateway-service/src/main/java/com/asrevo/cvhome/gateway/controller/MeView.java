package com.asrevo.cvhome.gateway.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.asrevo.cvhome.gateway.impersonation.ImpersonationView;

/**
 * {@code GET /api/v1/auth/me}, stated explicitly.
 *
 * <p>
 * The field names are the ones ui-kit's {@code AuthService} already reads off the serialised token this replaces:
 * {@code principal.claims.sub}, {@code principal.preferredUsername}, {@code principal.name}, the three profile
 * fields, and {@code authorities[].authority}. An OIDC principal fills them from the ID token; an impersonated one —
 * a plain {@code OAuth2User} built from the exchanged access token — from its attributes, which is exactly what the
 * serialised form could not have done.
 * </p>
 *
 * @param impersonation what the session is acting as, or {@code null} when it is itself
 */
public record MeView(PrincipalView principal, List<AuthorityView> authorities, ImpersonationView impersonation) {

    public static MeView of(OAuth2AuthenticationToken login, ImpersonationView impersonation) {
        OAuth2User user = login.getPrincipal();
        PrincipalView principal = user instanceof OidcUser oidc
                ? new PrincipalView(oidc.getClaims(), login.getName(), oidc.getPreferredUsername(), oidc.getGivenName(),
                        oidc.getFamilyName(), oidc.getEmail())
                : new PrincipalView(user.getAttributes(), login.getName(),
                        user.getAttribute(StandardClaimNames.PREFERRED_USERNAME),
                        user.getAttribute(StandardClaimNames.GIVEN_NAME), user.getAttribute(StandardClaimNames.FAMILY_NAME),
                        user.getAttribute(StandardClaimNames.EMAIL));
        List<AuthorityView> authorities = login.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).sorted().map(AuthorityView::new).toList();
        return new MeView(principal, authorities, impersonation);
    }

    /**
     * @param claims the token's claims; {@code sub} is the account id
     * @param name   the principal name — the account id for a login, a composite for an impersonation
     */
    public record PrincipalView(Map<String, Object> claims, String name, String preferredUsername, String givenName,
                                String familyName, String email) {
    }

    public record AuthorityView(String authority) {
    }

}
