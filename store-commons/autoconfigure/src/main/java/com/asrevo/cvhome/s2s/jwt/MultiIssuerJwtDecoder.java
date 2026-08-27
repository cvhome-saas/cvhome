package com.asrevo.cvhome.s2s.jwt;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * A {@link JwtDecoder} that accepts tokens from more than one identity server for synchronous environments.
 * The {@code iss} claim selects a realm through {@link IssuerRegistry}, and one delegate decoder is built and
 * cached per realm.
 *
 * <p>
 * The cache is keyed by realm rather than by issuer string: a realm answers on several URIs (an explicit
 * default port or not, a shifted port, a path prefix) and they all share one signing key set, so keying by URI
 * built the same decoder several times over.
 * </p>
 */
public class MultiIssuerJwtDecoder implements JwtDecoder {

    private static final String NULL_DECODER = """
            Decoder factory returned null for realm '%s'. \
            This indicates an issue with the factory configuration.""";

    private final Map<String, JwtDecoder> realmDecoders = new ConcurrentHashMap<>();

    private final IssuerRegistry registry;

    private final Function<IssuerRealm, JwtDecoder> decoderFactory;

    /**
     * @param registry       the realms this decoder trusts. Must not be null.
     * @param decoderFactory builds the delegate decoder for a realm, invoked lazily on first use and cached.
     *                       Must not be null, and should throw rather than return null when it cannot build one.
     */
    public MultiIssuerJwtDecoder(IssuerRegistry registry, Function<IssuerRealm, JwtDecoder> decoderFactory) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.decoderFactory = Objects.requireNonNull(decoderFactory, "decoderFactory cannot be null");
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        Objects.requireNonNull(token, "token cannot be null");
        try {
            IssuerRealm realm = this.registry.resolve(token);
            return decoderFor(realm).decode(token);
        } catch (JwtException e) {
            // Passes a BadJwtException through unchanged, which is what keeps a client's bad token a 401.
            throw e;
        } catch (Exception e) {
            // Building the delegate failed — JWKS or discovery unreachable. Ours to fix, so it stays a 500.
            throw new JwtException("Failed to decode JWT: %s".formatted(e.getMessage()), e);
        }
    }

    private JwtDecoder decoderFor(IssuerRealm realm) {
        JwtDecoder delegate = this.realmDecoders.computeIfAbsent(realm.name(), name -> this.decoderFactory.apply(realm));
        if (delegate == null) {
            throw new JwtException(NULL_DECODER.formatted(realm.name()));
        }
        return delegate;
    }

}
