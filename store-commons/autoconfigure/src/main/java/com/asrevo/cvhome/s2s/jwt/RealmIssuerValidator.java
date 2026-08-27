package com.asrevo.cvhome.s2s.jwt;

import java.util.Objects;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import com.asrevo.cvhome.s2s.utils.UrlNormalize;

/**
 * Asserts that a decoded token's {@code iss} really is one of its realm's, comparing normalized.
 *
 * <p>
 * This replaces {@code JwtValidators.createDefaultWithIssuer}, which compares one issuer string for exact
 * equality. That is too literal for a realm reachable at several equivalent URIs: a pod endpoint carrying an
 * explicit {@code :443} and the same endpoint without it are the same issuer, and only one of them can be the
 * string a decoder was pinned to.
 * </p>
 */
public final class RealmIssuerValidator implements OAuth2TokenValidator<Jwt> {

    private static final String INVALID_TOKEN = "invalid_token";

    private final IssuerRealm realm;

    public RealmIssuerValidator(IssuerRealm realm) {
        this.realm = Objects.requireNonNull(realm, "realm cannot be null");
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String issuer = token.getClaimAsString(JwtClaimNames.ISS);
        if (issuer != null && this.realm.issuerUris().contains(UrlNormalize.normalizeQuietly(issuer))) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error(INVALID_TOKEN,
                "The iss claim '%s' is not one of realm '%s': %s".formatted(issuer, this.realm.name(),
                        this.realm.issuerUris()),
                null));
    }

}
