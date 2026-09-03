package com.asrevo.cvhome.sso.invitation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * The bearer tokens behind invitation and reset links: 256 random bits, URL-safe, compared by SHA-256.
 *
 * <p>
 * The same shape tenancy's invitations use. A token is never logged, never stored and never returned by any read
 * — losing it means issuing another.
 * </p>
 */
public final class OneTimeTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int BYTES = 32;

    private OneTimeTokens() {
    }

    public static String newToken() {
        byte[] bytes = new byte[BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is required of every JVM; if it is genuinely missing, failing loudly is the only honest move.
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

}
