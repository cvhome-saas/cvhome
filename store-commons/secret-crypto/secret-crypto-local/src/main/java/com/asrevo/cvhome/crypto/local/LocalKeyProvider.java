package com.asrevo.cvhome.crypto.local;

import java.util.Optional;

/**
 * Internal SPI for providing keys to LocalAesCryptoProvider.
 */
public interface LocalKeyProvider {
    /**
     * Retrieves the key material for a given key ID.
     *
     * @param keyId The identifier of the key.
     * @return Optional containing the key bytes, or empty if not found.
     */
    Optional<byte[]> getKey(String keyId);
}
