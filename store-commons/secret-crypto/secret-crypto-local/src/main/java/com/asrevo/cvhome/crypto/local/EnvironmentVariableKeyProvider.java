package com.asrevo.cvhome.crypto.local;

import java.util.Base64;
import java.util.Optional;

/**
 * Obtains keys from environment variables.
 * Key ID 'key-1' will look for environment variable 'COM_ASREVO_CVHOME_CRYPTO_KEY_KEY_1'.
 */
public class EnvironmentVariableKeyProvider implements LocalKeyProvider {

    private static final String PREFIX = "COM_ASREVO_CVHOME_CRYPTO_KEY_";

    @Override
    public Optional<byte[]> getKey(String keyId) {
        String envVarName = PREFIX + keyId.toUpperCase().replace("-", "_");
        String value = System.getenv(envVarName);
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
