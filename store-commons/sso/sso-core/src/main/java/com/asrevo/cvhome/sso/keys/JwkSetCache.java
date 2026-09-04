package com.asrevo.cvhome.sso.keys;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWKSet;

/**
 * The JWK set every token operation reads, built once and rebuilt only when a key changes.
 *
 * <p>
 * Decrypting the active key's private half costs a crypto call; doing it per token would put the crypto provider on
 * the token endpoint's hot path. The set is rebuilt on rotation and retirement, which is when its contents change.
 * </p>
 */
@Component
public class JwkSetCache {

    private volatile JWKSet current;

    /** The set, loading it through {@code loader} when nothing is cached. */
    public JWKSet get(Supplier<JWKSet> loader) {
        JWKSet set = current;
        if (set == null) {
            synchronized (this) {
                set = current;
                if (set == null) {
                    set = loader.get();
                    current = set;
                }
            }
        }
        return set;
    }

    public void invalidate() {
        current = null;
    }

}
