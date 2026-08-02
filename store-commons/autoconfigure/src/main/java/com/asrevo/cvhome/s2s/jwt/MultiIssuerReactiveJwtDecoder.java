package com.asrevo.cvhome.s2s.jwt;

import java.text.ParseException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import reactor.core.publisher.Mono;

/**
 * A {@link ReactiveJwtDecoder} that supports multiple JWT issuers. It lazily initializes
 * and caches a specific {@link ReactiveJwtDecoder} for each issuer based on the 'iss'
 * claim in the JWT.
 */
public class MultiIssuerReactiveJwtDecoder implements ReactiveJwtDecoder {

    private final Map<String, ReactiveJwtDecoder> issuerDecoders = new ConcurrentHashMap<>();

    private final Set<String> supportedIssuerUris;

    private final Function<String, ReactiveJwtDecoder> decoderFactory;

    /**
     * Constructs a {@code MultiIssuerReactiveJwtDecoder}.
     *
     * @param supportedIssuerUris A set of trusted issuer URIs that this decoder will
     *                            handle. An attempt to decode a token from an issuer not in this set will result in
     *                            an error. Must not be null.
     * @param decoderFactory      A function that takes an issuer URI (String) and returns a
     *                            {@link ReactiveJwtDecoder} configured for that issuer. This factory is invoked
     *                            lazily when a token from a new, supported issuer is encountered, and its result is
     *                            cached. Must not be null. The factory should throw an exception if it cannot create
     *                            a decoder for a given supported issuer URI.
     */
    public MultiIssuerReactiveJwtDecoder(Set<String> supportedIssuerUris,
                                         Function<String, ReactiveJwtDecoder> decoderFactory) {
        Objects.requireNonNull(supportedIssuerUris, "supportedIssuerUris cannot be null");
        Objects.requireNonNull(decoderFactory, "decoderFactory cannot be null");

        this.supportedIssuerUris = supportedIssuerUris.stream()
                .map(UrlNormalize::normalizeUri)
                .collect(Collectors.toSet());
        this.decoderFactory = decoderFactory;
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        Objects.requireNonNull(token, "token cannot be null");
        return Mono.fromCallable(() -> extractIssuer(token))
                .flatMap(this::getDecoderForIssuer)
                .flatMap(decoder -> decoder.decode(token))
                .onErrorMap(ex -> {
                    if (ex instanceof JwtException) {
                        return ex;
                    }
                    return new JwtException(String.format("Failed to decode JWT: %s", ex.getMessage()), ex);
                });
    }

    /**
     * Retrieves or creates a {@link ReactiveJwtDecoder} for the given issuer.
     *
     * @param issuer The issuer URI.
     * @return A {@link Mono} emitting the {@link ReactiveJwtDecoder} or an error if the
     * issuer is unsupported or the factory fails.
     */
    private Mono<ReactiveJwtDecoder> getDecoderForIssuer(String issuer) {
        if (!this.supportedIssuerUris.contains(issuer)) {
            return Mono.error(new JwtException(String.format(
                    "Unsupported issuer: '%s'. Issuer not in the configured list of supported issuers: %s.",
                    issuer, this.supportedIssuerUris)));
        }

        ReactiveJwtDecoder delegateDecoder = this.issuerDecoders.computeIfAbsent(issuer, this.decoderFactory);

        if (delegateDecoder == null) {
            return Mono
                    .error(new JwtException(String.format(
                            "Decoder factory returned null for supported issuer: '%s'. This indicates an issue with the factory configuration.",
                            issuer)));
        }
        return Mono.just(delegateDecoder);
    }

    /**
     * Extracts the 'iss' (issuer) claim from the JWT.
     *
     * @param token The JWT string.
     * @return The issuer URI.
     * @throws JwtException if the token cannot be parsed or the 'iss' claim is missing or
     *                      empty.
     */
    private String extractIssuer(String token) throws JwtException {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            String issuer = claimsSet.getIssuer();
            if (issuer == null || issuer.trim().isEmpty()) {
                throw new JwtException("Missing or empty 'iss' (issuer) claim in token");
            }
            return issuer;
        } catch (ParseException e) {
            throw new JwtException(String.format("Failed to parse token to extract issuer: %s", e.getMessage()), e);
        }
    }

}
