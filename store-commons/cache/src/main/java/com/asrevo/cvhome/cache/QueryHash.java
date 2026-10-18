package com.asrevo.cvhome.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.asrevo.cvhome.commons.domain.KeyPart;

/**
 * A listing or search criteria as a cache key part: the SHA-256 of its normalised text, so two requests that mean
 * the same thing share one entry and a mutable criteria object never sits in a key.
 *
 * @param value the hash, 64 hex characters
 */
public record QueryHash(String value) implements KeyPart {

    private static final String ALGORITHM = "SHA-256";

    public QueryHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("a query hash is never blank");
        }
    }

    /** Hashes {@code normalised}: the criteria rendered by its owner with the fields sorted and the text trimmed. */
    public static QueryHash of(String normalised) {
        if (normalised == null) {
            throw new IllegalArgumentException("a query hash needs the normalised text");
        }
        try {
            byte[] digest = MessageDigest.getInstance(ALGORITHM).digest(normalised.getBytes(StandardCharsets.UTF_8));
            return new QueryHash(HexFormat.of().formatHex(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(String.format("%s is part of every JDK", ALGORITHM), e);
        }
    }

    @Override
    public String cacheKeyPart() {
        return value;
    }
}
