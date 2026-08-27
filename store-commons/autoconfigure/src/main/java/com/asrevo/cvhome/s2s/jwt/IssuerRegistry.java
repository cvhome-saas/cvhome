package com.asrevo.cvhome.s2s.jwt;

import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.oauth2.jwt.BadJwtException;

import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.nimbusds.jwt.SignedJWT;

/**
 * The trust decision behind {@link MultiIssuerJwtDecoder} and {@link MultiIssuerReactiveJwtDecoder}: which
 * identity server signed this token, and do we accept that server at all.
 *
 * <p>
 * Two rules make this survive deployment topology, which a flat set of issuer URIs did not.
 * </p>
 *
 * <p>
 * <strong>Issuers are compared normalized, never literally.</strong> Every configured URI and every presented
 * {@code iss} goes through {@link UrlNormalize#normalizeUri}, which drops a default port scheme-relatively. A
 * pod's cua issuer is built from {@code pod.endpoint().endpoint()}, an operator-entered free-form column in the
 * pod registry that nothing canonicalises on the way in — so whether it reads {@code https://host} or
 * {@code https://host:443} is arbitrary and differs between environments. Comparing the two literally rejected
 * every shopper token the pod issued, in whichever environment happened to disagree.
 * </p>
 *
 * <p>
 * <strong>A token we will not accept fails as {@link BadJwtException}.</strong> Spring's
 * {@code JwtAuthenticationProvider} maps that to an {@code InvalidBearerTokenException} and a 401; a bare
 * {@code JwtException} it maps to an {@code AuthenticationServiceException}, which
 * {@code AuthenticationEntryPointFailureHandler} deliberately rethrows — escaping the filter chain as a 500,
 * past every {@code @ControllerAdvice}. An unparseable token, a missing {@code iss} and an untrusted {@code iss}
 * are all the client's fault and must read as 401. A bare {@code JwtException} is reserved for our own
 * infrastructure failing, which is the one case a 500 describes honestly.
 * </p>
 */
public final class IssuerRegistry {

    private final Map<String, IssuerRealm> byIssuer = new LinkedHashMap<>();

    private final List<IssuerRealm> realms;

    public IssuerRegistry(Collection<IssuerRealm> realms) {
        Objects.requireNonNull(realms, "realms cannot be null");
        this.realms = List.copyOf(realms);
        for (IssuerRealm realm : this.realms) {
            for (String issuer : realm.issuerUris()) {
                IssuerRealm clash = this.byIssuer.putIfAbsent(issuer, realm);
                if (clash != null && !clash.name().equals(realm.name())) {
                    throw new IllegalArgumentException("Issuer '%s' is claimed by both realm '%s' and realm '%s'"
                            .formatted(issuer, clash.name(), realm.name()));
                }
            }
        }
    }

    public List<IssuerRealm> realms() {
        return this.realms;
    }

    public Set<String> supportedIssuerUris() {
        return this.byIssuer.keySet();
    }

    /** The realm that issued a decoded token, if any. Used to decide what authority it may confer. */
    public Optional<IssuerRealm> findByIssuer(String issuer) {
        if (issuer == null || issuer.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.byIssuer.get(UrlNormalize.normalizeQuietly(issuer)));
    }

    /**
     * The realm that signed {@code token}, or a {@link BadJwtException} saying why we will not accept it.
     *
     * <p>
     * The token is parsed without verifying its signature, which is safe: the {@code iss} claim is used only to
     * select a decoder, and that decoder then verifies the signature for real. An unknown issuer never reaches
     * one.
     * </p>
     */
    public IssuerRealm resolve(String token) {
        String issuer = extractIssuer(token);
        return findByIssuer(issuer).orElseThrow(() -> new BadJwtException(
                "Unsupported issuer: '%s'. Issuer not in the configured list of supported issuers: %s."
                        .formatted(issuer, this.byIssuer.keySet())));
    }

    private static String extractIssuer(String token) {
        try {
            String issuer = SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
            if (issuer == null || issuer.trim().isEmpty()) {
                throw new BadJwtException("Missing or empty 'iss' (issuer) claim in token");
            }
            return issuer;
        } catch (ParseException e) {
            throw new BadJwtException("Failed to parse token to extract issuer: %s".formatted(e.getMessage()), e);
        }
    }

}
