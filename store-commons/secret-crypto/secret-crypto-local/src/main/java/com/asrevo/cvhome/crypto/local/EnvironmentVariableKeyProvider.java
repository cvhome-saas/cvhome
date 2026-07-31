package com.asrevo.cvhome.crypto.local;

import java.util.Base64;
import java.util.Optional;

/**
 * Obtains key from environment variables.
 * Key  will look for environment variable 'COM_ASREVO_CVHOME_CRYPTO_KEY'.
 */
public class EnvironmentVariableKeyProvider implements LocalKeyProvider {

    private static final String PREFIX = "COM_ASREVO_CVHOME_CRYPTO_KEY";

    @Override
    public Optional<byte[]> getKey() {
        String value = System.getenv(PREFIX);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Base64.getDecoder().decode(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
