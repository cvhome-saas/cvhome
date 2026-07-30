package com.asrevo.cvhome.payment.mapper;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentConfigurationMapper {

    private final SecretCryptoProvider cryptoProvider;

    public PaymentConfiguration toEntity(PersistablePaymentConfiguration dto) {
        if (dto == null) {
            return null;
        }

        PaymentConfiguration entity = new PaymentConfiguration();
        entity.setId(new PaymentConfigurationId(dto.getStoreMerchantId(), dto.getPaymentType()));
        entity.setEnabled(dto.isEnabled());

        if (dto.getApiKey() != null && !EncryptedValue.isEncrypted(dto.getApiKey())) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getApiKey().getBytes(StandardCharsets.UTF_8));
            entity.setApiKey(encrypted.serialize());
        }

        if (dto.getSecretKey() != null && !EncryptedValue.isEncrypted(dto.getSecretKey())) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getSecretKey().getBytes(StandardCharsets.UTF_8));
            entity.setSecretKey(encrypted.serialize());
        }

        if (dto.getWebhookSecret() != null && !EncryptedValue.isEncrypted(dto.getWebhookSecret())) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getWebhookSecret().getBytes(StandardCharsets.UTF_8));
            entity.setWebhookSecret(encrypted.serialize());
        }

        return entity;
    }

    public ReadablePaymentConfiguration toDTO(PaymentConfiguration entity) {
        if (entity == null) {
            return null;
        }

        ReadablePaymentConfiguration dto = new ReadablePaymentConfiguration();
        dto.setStoreMerchantId(entity.getId().getStoreMerchantId());
        dto.setPaymentType(entity.getId().getPaymentType());
        dto.setEnabled(entity.isEnabled());

        dto.setApiKey(decrypt(entity.getApiKey()));
        dto.setSecretKey(decrypt(entity.getSecretKey()));
        dto.setWebhookSecret(decrypt(entity.getWebhookSecret()));

        return dto;
    }

    private String decrypt(String value) {
        if (EncryptedValue.isEncrypted(value)) {
                try {
                    byte[] decrypted = cryptoProvider.decrypt(EncryptedValue.deserialize(value));
                    return new String(decrypted, StandardCharsets.UTF_8);
                } catch (Exception _) {
                    log.error("Failed to decrypt value: {}", value);
            }
        }
        return null;
    }

    public void updateEntity(PaymentConfiguration entity, PersistablePaymentConfiguration dto) {
        if (dto == null || entity == null) {
            return;
        }

        entity.setEnabled(dto.isEnabled());

        if (dto.getApiKey() != null) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getApiKey().getBytes(StandardCharsets.UTF_8));
            entity.setApiKey(encrypted.serialize());
        }

        if (dto.getSecretKey() != null) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getSecretKey().getBytes(StandardCharsets.UTF_8));
            entity.setSecretKey(encrypted.serialize());
        }

        if (dto.getWebhookSecret() != null) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getWebhookSecret().getBytes(StandardCharsets.UTF_8));
            entity.setWebhookSecret(encrypted.serialize());
        }
    }
}
