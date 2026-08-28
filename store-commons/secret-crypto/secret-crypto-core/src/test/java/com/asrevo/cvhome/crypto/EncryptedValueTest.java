package com.asrevo.cvhome.crypto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The envelope that every encrypted secret is stored as, and the only contract a database row has to satisfy to be
 * readable again.
 *
 * <p>
 * The tests are about what happens to a row that is <em>not</em> a well-formed envelope, because that is what a
 * migration, a hand-edited fixture or a provider that never filled a field actually produces. A malformed row must
 * announce itself; the one thing it must never do is decode into something plausible.
 * </p>
 */
class EncryptedValueTest {

    private static final String KEY_ID = "default-key";

    private static final String ALGORITHM = "AES-256-GCM";

    private static final byte[] CIPHERTEXT = "ciphertext".getBytes(StandardCharsets.UTF_8);

    private static final byte[] IV = "123456789012".getBytes(StandardCharsets.UTF_8);

    private static final String PLAINTEXT_SECRET = "sk_live_plaintext";

    private static EncryptedValue.EncryptedValueBuilder envelope() {
        return EncryptedValue.builder().version(1).keyId(KEY_ID).algorithm(ALGORITHM).ciphertext(CIPHERTEXT).iv(IV);
    }

    @Nested
    class IsEncrypted {

        @Test
        void nullIsNotEncrypted() {
            assertThat(EncryptedValue.isEncrypted(null)).isFalse();
        }

        @Test
        void aValueWithoutThePrefixIsNotEncrypted() {
            assertThat(EncryptedValue.isEncrypted(PLAINTEXT_SECRET)).isFalse();
        }

        @Test
        void aValueCarryingThePrefixIsEncrypted() {
            assertThat(EncryptedValue.isEncrypted("ENC:1:default-key:AES-256-GCM:aXY=:Y3Q=")).isTrue();
        }
    }

    @Nested
    class Deserialize {

        @Test
        void nullDeserializesToNullRatherThanThrowing() {
            assertThat(EncryptedValue.deserialize(null)).isNull();
        }

        @Test
        void emptyDeserializesToNullRatherThanThrowing() {
            assertThat(EncryptedValue.deserialize("")).isNull();
        }

        @Test
        void aPlaintextValueIsRejectedInsteadOfBeingReadAsAnEnvelope() {
            assertThatThrownBy(() -> EncryptedValue.deserialize(PLAINTEXT_SECRET))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not encrypted");
        }

        @Test
        void anEnvelopeMissingAPartIsRejected() {
            assertThatThrownBy(() -> EncryptedValue.deserialize("ENC:1:default-key:AES-256-GCM:aXY="))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("expected 5 parts");
        }

        @Test
        void everyFieldSurvivesARoundTrip() {
            EncryptedValue restored = EncryptedValue.deserialize(envelope().build().serialize());

            assertThat(restored.getVersion()).isEqualTo(1);
            assertThat(restored.getKeyId()).isEqualTo(KEY_ID);
            assertThat(restored.getAlgorithm()).isEqualTo(ALGORITHM);
            assertThat(restored.getCiphertext()).isEqualTo(CIPHERTEXT);
            assertThat(restored.getIv()).isEqualTo(IV);
        }
    }

    @Nested
    class Serialize {

        @Test
        void theSerializedFormIsThePrefixedFiveFieldRecordTheReaderExpects() {
            String serialized = envelope().build().serialize();

            assertThat(serialized).isEqualTo(String.format("ENC:1:%s:%s:%s:%s", KEY_ID, ALGORITHM,
                    Base64.getEncoder().encodeToString(IV), Base64.getEncoder().encodeToString(CIPHERTEXT)));
        }

        /**
         * A provider that derives its own IV — AWS KMS does — leaves the field null. Serializing that used to throw
         * a NullPointerException, so a KMS-encrypted secret could not be written to a row at all, and had it been
         * written it would have carried four fields where the reader demands five.
         */
        /**
         * A KMS key id is an ARN, and an ARN is mostly colons. Splitting the record into a fixed five fields tore one
         * apart, so a KMS-encrypted secret could be written and never read back.
         */
        @Test
        void aKeyIdContainingSeparatorsSurvivesTheRoundTrip() {
            String arn = "arn:aws:kms:eu-west-1:111122223333:key/1234abcd";

            EncryptedValue restored = EncryptedValue.deserialize(envelope().keyId(arn).build().serialize());

            assertThat(restored.getKeyId()).isEqualTo(arn);
            assertThat(restored.getAlgorithm()).isEqualTo(ALGORITHM);
            assertThat(restored.getCiphertext()).isEqualTo(CIPHERTEXT);
        }

        @Test
        void anEnvelopeWithoutAnIvStillRoundTrips() {
            EncryptedValue withoutIv = envelope().iv(null).build();

            EncryptedValue restored = EncryptedValue.deserialize(withoutIv.serialize());

            assertThat(restored.getIv()).isEmpty();
            assertThat(restored.getCiphertext()).isEqualTo(CIPHERTEXT);
            assertThat(restored.getKeyId()).isEqualTo(KEY_ID);
        }
    }
}
