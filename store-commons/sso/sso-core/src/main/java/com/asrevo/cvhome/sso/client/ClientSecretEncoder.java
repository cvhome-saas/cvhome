package com.asrevo.cvhome.sso.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * How a client secret is stored and checked: a salted SHA-256, {@code {sha256}<salt>$<digest>}, not bcrypt.
 *
 * <p>
 * bcrypt's work factor exists to slow down guessing a low-entropy human password after a database leak. A client
 * secret is not one: uaa generates 32 random bytes on create and rotate, the platform's bootstrap generates the seeded
 * ones, and a configured secret shorter than {@link #MIN_LENGTH} characters stops startup. Guessing 256 random bits is
 * infeasible whatever the hash costs, which is the argument {@code OneTimeTokens} already makes for invitation tokens.
 * bcrypt at strength 12 cost 227 ms of CPU on every token request, and a seller's sign-in makes one.
 * </p>
 *
 * <p>
 * A hash written before this, {@code {bcrypt}$2a$...} or a bare {@code $2a$...}, still verifies, and
 * {@link #upgradeEncoding} answers {@code true} for it, so Spring Authorization Server's client authentication rewrites
 * it as {@code {sha256}} the first time the client authenticates. Anything else fails closed: an unknown prefix or a
 * malformed value is a mismatch, never an exception.
 * </p>
 *
 * <p>
 * Deliberately not a {@link PasswordEncoder}: a by-type {@code PasswordEncoder} injection keeps meaning the password
 * encoder. {@link #asPasswordEncoder()} hands the delegating encoder to the one place that needs the interface,
 * Spring's client-secret provider.
 * </p>
 */
public final class ClientSecretEncoder {

    /** The shortest client secret this server accepts; a generated one is 43 characters. */
    public static final int MIN_LENGTH = 32;

    static final String SHA256 = "sha256";

    private static final String BCRYPT = "bcrypt";

    private final PasswordEncoder delegate;

    public ClientSecretEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        DelegatingPasswordEncoder delegating = new DelegatingPasswordEncoder(SHA256,
                Map.of(SHA256, new SaltedSha256(), BCRYPT, bcrypt));
        // A bare $2a$ hash, or an id nobody maps, goes to bcrypt, which answers false for anything that is not bcrypt:
        // the stock behaviour would throw instead.
        delegating.setDefaultPasswordEncoderForMatches(bcrypt);
        this.delegate = delegating;
    }

    public String encode(CharSequence secret) {
        return delegate.encode(secret);
    }

    public boolean matches(CharSequence secret, String encoded) {
        return encoded != null && delegate.matches(secret, encoded);
    }

    public boolean upgradeEncoding(String encoded) {
        return delegate.upgradeEncoding(encoded);
    }

    /** The delegating encoder, for Spring's {@code ClientSecretAuthenticationProvider}. */
    public PasswordEncoder asPasswordEncoder() {
        return delegate;
    }

    /** {@code <salt>$<digest>}, both standard Base64, after the {@code {sha256}} prefix the delegating encoder adds. */
    static final class SaltedSha256 implements PasswordEncoder {

        private static final int SALT_BYTES = 16;

        private static final char SEPARATOR = '$';

        private final SecureRandom random = new SecureRandom();

        @Override
        public String encode(CharSequence secret) {
            byte[] salt = new byte[SALT_BYTES];
            random.nextBytes(salt);
            Base64.Encoder base64 = Base64.getEncoder();
            return base64.encodeToString(salt) + SEPARATOR + base64.encodeToString(digest(salt, secret));
        }

        @Override
        public boolean matches(CharSequence secret, String encoded) {
            if (secret == null || encoded == null) {
                return false;
            }
            int at = encoded.indexOf(SEPARATOR);
            if (at <= 0 || at != encoded.lastIndexOf(SEPARATOR) || at == encoded.length() - 1) {
                return false;
            }
            try {
                Base64.Decoder base64 = Base64.getDecoder();
                byte[] salt = base64.decode(encoded.substring(0, at));
                byte[] expected = base64.decode(encoded.substring(at + 1));
                return MessageDigest.isEqual(expected, digest(salt, secret));
            } catch (IllegalArgumentException malformed) {
                return false;
            }
        }

        private static byte[] digest(byte[] salt, CharSequence secret) {
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                sha256.update(salt);
                return sha256.digest(secret.toString().getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                // SHA-256 is required of every JVM; if it is genuinely missing, failing loudly is the only honest move.
                throw new IllegalStateException("SHA-256 is unavailable", e);
            }
        }
    }
}
