package com.asrevo.cvhome.sso.client;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * A client secret is stored as a salted SHA-256; an older bcrypt hash still verifies and asks to be rewritten; anything
 * malformed is a mismatch, never an exception.
 */
class ClientSecretEncoderTest {

    private static final String SECRET = "hLwOF59NEOdMzYYrfxUbQEGVK1uTczj7";

    private static final String OTHER = "hLwOF59NEOdMzYYrfxUbQEGVK1uTczj8";

    private static final String SHA256_PREFIX = "{sha256}";

    /** How the password encoder this replaces wrote a client secret: a prefixed bcrypt hash. */
    private static final String PREFIXED_BCRYPT = "{bcrypt}%s";

    /** Strength 4 keeps the test fast; the strength a hash was written with does not change how it verifies. */
    private static final BCryptPasswordEncoder OLD_BCRYPT = new BCryptPasswordEncoder(4);

    private final ClientSecretEncoder encoder = new ClientSecretEncoder();

    @Test
    void aSecretRoundTripsThroughTheSha256Form() {
        String encoded = encoder.encode(SECRET);

        assertThat(encoded).startsWith(SHA256_PREFIX).doesNotContain(SECRET);
        assertThat(encoder.matches(SECRET, encoded)).isTrue();
    }

    @Test
    void twoEncodingsOfOneSecretDifferBecauseEachHasItsOwnSalt() {
        String first = encoder.encode(SECRET);
        String second = encoder.encode(SECRET);

        assertThat(first).isNotEqualTo(second);
        assertThat(encoder.matches(SECRET, first)).isTrue();
        assertThat(encoder.matches(SECRET, second)).isTrue();
    }

    @Test
    void aWrongSecretDoesNotMatch() {
        assertThat(encoder.matches(OTHER, encoder.encode(SECRET))).isFalse();
    }

    static Stream<String> malformed() {
        return Stream.of("", "garbage", SHA256_PREFIX, "{sha256}no-separator", "{sha256}$", "{sha256}c2FsdA==$",
                "{sha256}$ZGlnZXN0", "{sha256}!!!$***", "{sha256}c2FsdA==$ZGln$ZXN0", "{unknown}whatever",
                "{noop}hLwOF59NEOdMzYYrfxUbQEGVK1uTczj7");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("malformed")
    void aMalformedStoredValueFailsClosedRatherThanThrowing(String stored) {
        assertThatCode(() -> assertThat(encoder.matches(SECRET, stored)).isFalse()).doesNotThrowAnyException();
    }

    @Test
    void anAbsentStoredValueDoesNotMatch() {
        assertThat(encoder.matches(SECRET, null)).isFalse();
    }

    @Test
    void aBcryptHashWrittenBeforeThisStillVerifiesWithOrWithoutItsPrefix() {
        String bare = OLD_BCRYPT.encode(SECRET);

        assertThat(encoder.matches(SECRET, String.format(PREFIXED_BCRYPT, bare))).isTrue();
        assertThat(encoder.matches(SECRET, bare)).isTrue();
        assertThat(encoder.matches(OTHER, String.format(PREFIXED_BCRYPT, bare))).isFalse();
    }

    @Test
    void aBcryptHashAsksToBeRewrittenAndAsha256OneDoesNot() {
        String bare = OLD_BCRYPT.encode(SECRET);

        assertThat(encoder.upgradeEncoding(String.format(PREFIXED_BCRYPT, bare))).isTrue();
        assertThat(encoder.upgradeEncoding(bare)).isTrue();
        assertThat(encoder.upgradeEncoding(encoder.encode(SECRET))).isFalse();
    }

    @Test
    void theInterfaceHandedToSpringIsTheSameEncoder() {
        String encoded = encoder.asPasswordEncoder().encode(SECRET);

        assertThat(encoded).startsWith(SHA256_PREFIX);
        assertThat(encoder.matches(SECRET, encoded)).isTrue();
    }

    @Test
    void theFloorMatchesWhatUaaGenerates() {
        // 32 random bytes, Base64URL without padding, is 43 characters; the floor must never refuse a generated one.
        assertThat(ClientSecretEncoder.MIN_LENGTH).isLessThanOrEqualTo(43);
    }
}
