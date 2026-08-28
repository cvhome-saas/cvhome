package com.asrevo.cvhome.s2s.jwt;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import reactor.core.publisher.Mono;

/**
 * The reactive twin of {@link MultiIssuerJwtDecoder}, for the gateway. Both delegate the trust decision to
 * {@link IssuerRegistry} rather than repeating it, which is what let a normalization fix land on one of them and
 * not the other.
 */
public class MultiIssuerReactiveJwtDecoder implements ReactiveJwtDecoder {

    private static final String NULL_DECODER = """
            Decoder factory returned null for realm '%s'. \
            This indicates an issue with the factory configuration.""";

    private final Map<String, ReactiveJwtDecoder> realmDecoders = new ConcurrentHashMap<>();

    private final IssuerRegistry registry;

    private final Function<IssuerRealm, ReactiveJwtDecoder> decoderFactory;

    /**
     * @param registry       the realms this decoder trusts. Must not be null.
     * @param decoderFactory builds the delegate decoder for a realm, invoked lazily on first use and cached.
     *                       Must not be null, and should throw rather than return null when it cannot build one.
     */
    public MultiIssuerReactiveJwtDecoder(IssuerRegistry registry,
                                         Function<IssuerRealm, ReactiveJwtDecoder> decoderFactory) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.decoderFactory = Objects.requireNonNull(decoderFactory, "decoderFactory cannot be null");
    }

    @Override
    public Mono<Jwt> decode(String token) throws JwtException {
        Objects.requireNonNull(token, "token cannot be null");
        return Mono.fromCallable(() -> this.registry.resolve(token))
                .map(this::decoderFor)
                .flatMap(decoder -> decoder.decode(token))
                .onErrorMap(ex -> {
                    if (ex instanceof JwtException) {
                        // A BadJwtException must survive as itself, or the client's 401 becomes our 500.
                        return ex;
                    }
                    return new JwtException("Failed to decode JWT: %s".formatted(ex.getMessage()), ex);
                });
    }

    private ReactiveJwtDecoder decoderFor(IssuerRealm realm) {
        ReactiveJwtDecoder delegate = this.realmDecoders.computeIfAbsent(realm.name(),
                name -> this.decoderFactory.apply(realm));
        if (delegate == null) {
            throw new JwtException(NULL_DECODER.formatted(realm.name()));
        }
        return delegate;
    }

}
