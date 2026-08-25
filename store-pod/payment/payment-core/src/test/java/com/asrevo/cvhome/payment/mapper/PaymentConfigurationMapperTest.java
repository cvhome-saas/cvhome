package com.asrevo.cvhome.payment.mapper;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Secrets are encrypted on the way in and decrypted on the way out; a value that is already ciphertext is never
 * encrypted twice, and a value that cannot be decrypted (a plaintext legacy row, a rotated key) reads as absent rather
 * than as garbage.
 */
@ExtendWith(MockitoExtension.class)
class PaymentConfigurationMapperTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String API_KEY = "pk_test";

    private static final String SECRET_KEY = "sk_test";

    private static final String WEBHOOK_SECRET = "whsec_test";

    private static final String KEY_ID = "k1";

    private static final String ALGORITHM = "AES-256-GCM";

    private static final String ENC_PREFIX = "ENC:";

    @Mock
    private SecretCryptoProvider crypto;

    private PaymentConfigurationMapper mapper;

    /** A reversible fake: the "ciphertext" is the plaintext, so the wire format is real but readable. */
    private static EncryptedValue fakeEncrypt(byte[] plaintext) {
        return EncryptedValue.builder().version(1).keyId(KEY_ID).algorithm(ALGORITHM).iv(new byte[12])
                .ciphertext(plaintext).build();
    }

    @BeforeEach
    void setUp() {
        mapper = new PaymentConfigurationMapper(crypto);
    }

    @Test
    void nullsMapToNulls() {
        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toDTO(null)).isNull();
        mapper.updateEntity(null, new PersistablePaymentConfiguration());
        mapper.updateEntity(new PaymentConfiguration(), null);
        verify(crypto, never()).encrypt(any());
    }

    @Test
    void plaintextSecretsAreEncryptedOnTheWayIn() {
        when(crypto.encrypt(any())).thenAnswer(inv -> fakeEncrypt(inv.getArgument(0)));
        PersistablePaymentConfiguration dto = PersistablePaymentConfiguration.builder().storeMerchantId(STORE)
                .paymentType(PaymentType.STRIPE).apiKey(API_KEY).secretKey(SECRET_KEY).webhookSecret(WEBHOOK_SECRET)
                .enabled(true).build();

        PaymentConfiguration entity = mapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(new PaymentConfigurationId(STORE, PaymentType.STRIPE));
        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getApiKey()).startsWith(ENC_PREFIX).contains(KEY_ID);
        assertThat(entity.getSecretKey()).startsWith(ENC_PREFIX);
        assertThat(entity.getWebhookSecret()).startsWith(ENC_PREFIX);
        assertThat(EncryptedValue.deserialize(entity.getSecretKey()).getCiphertext())
                .isEqualTo(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void alreadyEncryptedOrAbsentSecretsAreLeftAloneOnTheWayIn() {
        String cipher = fakeEncrypt(API_KEY.getBytes(StandardCharsets.UTF_8)).serialize();
        PersistablePaymentConfiguration dto = PersistablePaymentConfiguration.builder().storeMerchantId(STORE)
                .paymentType(PaymentType.COD).apiKey(cipher).build();

        PaymentConfiguration entity = mapper.toEntity(dto);

        assertThat(entity.getApiKey()).isNull();
        assertThat(entity.getSecretKey()).isNull();
        assertThat(entity.getWebhookSecret()).isNull();
        verify(crypto, never()).encrypt(any());
    }

    @Test
    void encryptedSecretsAreDecryptedOnTheWayOut() {
        when(crypto.decrypt(any())).thenAnswer(inv -> inv.<EncryptedValue>getArgument(0).getCiphertext());
        PaymentConfiguration entity = new PaymentConfiguration();
        entity.setId(new PaymentConfigurationId(STORE, PaymentType.STRIPE));
        entity.setEnabled(true);
        entity.setApiKey(fakeEncrypt(API_KEY.getBytes(StandardCharsets.UTF_8)).serialize());
        entity.setSecretKey(fakeEncrypt(SECRET_KEY.getBytes(StandardCharsets.UTF_8)).serialize());
        entity.setWebhookSecret(fakeEncrypt(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8)).serialize());

        ReadablePaymentConfiguration dto = mapper.toDTO(entity);

        assertThat(dto.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(dto.getPaymentType()).isEqualTo(PaymentType.STRIPE);
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getApiKey()).isEqualTo(API_KEY);
        assertThat(dto.getSecretKey()).isEqualTo(SECRET_KEY);
        assertThat(dto.getWebhookSecret()).isEqualTo(WEBHOOK_SECRET);
    }

    @Test
    void plaintextRowsAndUndecryptableValuesReadAsAbsent() {
        when(crypto.decrypt(any())).thenThrow(new IllegalStateException("wrong key"));
        PaymentConfiguration entity = new PaymentConfiguration();
        entity.setId(new PaymentConfigurationId(STORE, PaymentType.PAYPAL));
        entity.setApiKey(API_KEY);
        entity.setSecretKey(fakeEncrypt(SECRET_KEY.getBytes(StandardCharsets.UTF_8)).serialize());

        ReadablePaymentConfiguration dto = mapper.toDTO(entity);

        assertThat(dto.getApiKey()).isNull();
        assertThat(dto.getSecretKey()).isNull();
        assertThat(dto.getWebhookSecret()).isNull();
    }

    @Test
    void updateReplacesOnlyTheSecretsThatWereSent() {
        when(crypto.encrypt(any())).thenAnswer(inv -> fakeEncrypt(inv.getArgument(0)));
        PaymentConfiguration entity = new PaymentConfiguration();
        entity.setApiKey(API_KEY);
        entity.setSecretKey(SECRET_KEY);
        entity.setWebhookSecret(WEBHOOK_SECRET);
        PersistablePaymentConfiguration dto = PersistablePaymentConfiguration.builder().secretKey("sk_new")
                .enabled(true).build();

        mapper.updateEntity(entity, dto);

        assertThat(entity.isEnabled()).isTrue();
        assertThat(entity.getApiKey()).isEqualTo(API_KEY);
        assertThat(entity.getWebhookSecret()).isEqualTo(WEBHOOK_SECRET);
        assertThat(entity.getSecretKey()).startsWith(ENC_PREFIX);
    }

    @Test
    void updateEncryptsEverySentSecret() {
        when(crypto.encrypt(any())).thenAnswer(inv -> fakeEncrypt(inv.getArgument(0)));
        PaymentConfiguration entity = new PaymentConfiguration();
        PersistablePaymentConfiguration dto = PersistablePaymentConfiguration.builder().apiKey(API_KEY)
                .secretKey(SECRET_KEY).webhookSecret(WEBHOOK_SECRET).build();

        mapper.updateEntity(entity, dto);

        assertThat(entity.getApiKey()).startsWith(ENC_PREFIX);
        assertThat(entity.getSecretKey()).startsWith(ENC_PREFIX);
        assertThat(entity.getWebhookSecret()).startsWith(ENC_PREFIX);
        assertThat(entity.isEnabled()).isFalse();
    }

}
