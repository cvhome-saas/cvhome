package com.asrevo.cvhome.crypto.local;

import java.util.Optional;

/**
 * Internal SPI for providing keys to LocalAesCryptoProvider.
 */
public interface LocalKeyProvider {
    /**
     * Retrieves the key material for a given key ID.
     *
     * @return Optional containing the key bytes, or empty if not found.
     */
    Optional<byte[]> getKey();
}
