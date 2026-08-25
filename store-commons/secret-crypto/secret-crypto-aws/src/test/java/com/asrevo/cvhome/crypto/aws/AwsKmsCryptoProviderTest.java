package com.asrevo.cvhome.crypto.aws;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.crypto.EncryptedValue;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;
import software.amazon.awssdk.services.kms.model.KmsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The KMS-backed provider, where the key never leaves AWS and the ciphertext blob is opaque to us.
 *
 * <p>
 * The point worth pinning is which key id each direction uses. Encryption uses the configured key, but decryption
 * has to use the key recorded in the envelope — otherwise rotating the configured key would strand every secret
 * written before the rotation.
 * </p>
 */
class AwsKmsCryptoProviderTest {

    private static final String CONFIGURED_KEY = "arn:aws:kms:eu-west-1:1:key/current";

    private static final String ENVELOPE_KEY = "arn:aws:kms:eu-west-1:1:key/retired";

    private static final byte[] SECRET = "sk_live_51H".getBytes(StandardCharsets.UTF_8);

    private static final byte[] BLOB = "opaque-kms-blob".getBytes(StandardCharsets.UTF_8);

    private KmsClient kms;

    private AwsKmsCryptoProvider provider;

    @BeforeEach
    void setUp() {
        kms = mock(KmsClient.class);
        provider = new AwsKmsCryptoProvider(kms, CONFIGURED_KEY);
    }

    @Test
    void encryptSendsThePlaintextUnderTheConfiguredKey() {
        when(kms.encrypt(any(EncryptRequest.class)))
                .thenReturn(EncryptResponse.builder().ciphertextBlob(SdkBytes.fromByteArray(BLOB)).build());
        ArgumentCaptor<EncryptRequest> request = ArgumentCaptor.forClass(EncryptRequest.class);

        provider.encrypt(SECRET);

        verify(kms).encrypt(request.capture());
        assertThat(request.getValue().keyId()).isEqualTo(CONFIGURED_KEY);
        assertThat(request.getValue().plaintext().asByteArray()).isEqualTo(SECRET);
    }

    @Test
    void theEnvelopeCarriesTheBlobAndTheAlgorithmTheRegistryRoutesBy() {
        when(kms.encrypt(any(EncryptRequest.class)))
                .thenReturn(EncryptResponse.builder().ciphertextBlob(SdkBytes.fromByteArray(BLOB)).build());

        EncryptedValue encrypted = provider.encrypt(SECRET);

        assertThat(encrypted.getAlgorithm()).isEqualTo(AwsKmsCryptoProvider.PROVIDER_ID);
        assertThat(encrypted.getKeyId()).isEqualTo(CONFIGURED_KEY);
        assertThat(encrypted.getCiphertext()).isEqualTo(BLOB);
    }

    /**
     * KMS derives its own IV inside the blob, so the envelope has none. It still has to serialize — see
     * {@code EncryptedValueTest}, where leaving this null used to throw.
     */
    @Test
    void aKmsEnvelopeCarriesNoIvAndStillSerializes() {
        when(kms.encrypt(any(EncryptRequest.class)))
                .thenReturn(EncryptResponse.builder().ciphertextBlob(SdkBytes.fromByteArray(BLOB)).build());

        EncryptedValue encrypted = provider.encrypt(SECRET);

        assertThat(encrypted.getIv()).isNull();
        assertThat(EncryptedValue.deserialize(encrypted.serialize()).getCiphertext()).isEqualTo(BLOB);
    }

    @Test
    void decryptUsesTheKeyRecordedInTheEnvelopeNotTheConfiguredOne() {
        when(kms.decrypt(any(DecryptRequest.class)))
                .thenReturn(DecryptResponse.builder().plaintext(SdkBytes.fromByteArray(SECRET)).build());
        ArgumentCaptor<DecryptRequest> request = ArgumentCaptor.forClass(DecryptRequest.class);

        byte[] plaintext = provider.decrypt(
                EncryptedValue.builder().keyId(ENVELOPE_KEY).ciphertext(BLOB).build());

        verify(kms).decrypt(request.capture());
        assertThat(request.getValue().keyId()).isEqualTo(ENVELOPE_KEY);
        assertThat(request.getValue().ciphertextBlob().asByteArray()).isEqualTo(BLOB);
        assertThat(plaintext).isEqualTo(SECRET);
    }

    @Test
    void aKmsFailureReachesTheCallerRatherThanBecomingAnEmptySecret() {
        when(kms.encrypt(any(EncryptRequest.class))).thenThrow(KmsException.builder().message("denied").build());

        assertThatThrownBy(() -> provider.encrypt(SECRET)).isInstanceOf(KmsException.class);
    }

    @Test
    void theProviderIdIsTheOneRecordedInEveryEnvelopeItProduces() {
        assertThat(provider.providerId()).isEqualTo("AWS-KMS");
    }
}
