package com.asrevo.cvhome.cua.web.mapper;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.crypto.EncryptedValue;
import com.asrevo.cvhome.crypto.SecretCryptoProvider;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.web.dto.PersistableSocialLoginConfig;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLoginConfig;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SocialLoginConfigMapper {

    private final SecretCryptoProvider cryptoProvider;

    public SocialLoginConfig toEntity(PersistableSocialLoginConfig dto) {
        if (dto == null) {
            return null;
        }

        SocialLoginConfig entity = new SocialLoginConfig();
        entity.setId(new SocialLoginConfigId(dto.getStoreMerchantId(), dto.getProviderId()));

        // Encrypt appId
        if (dto.getAppId() != null) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getAppId().getBytes(StandardCharsets.UTF_8));
            entity.setAppId(encrypted.serialize());
        }

        // Encrypt appSecret
        if (dto.getAppSecret() != null) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getAppSecret().getBytes(StandardCharsets.UTF_8));
            entity.setAppSecret(encrypted.serialize());
        }

        entity.setEnabled(dto.getEnabled());
        return entity;
    }

    public ReadableSocialLoginConfig toDTO(SocialLoginConfig entity) {
        if (entity == null) {
            return null;
        }

        ReadableSocialLoginConfig dto = new ReadableSocialLoginConfig();
        dto.setStoreMerchantId(entity.getId().storeMerchantId());
        dto.setProviderId(entity.getId().providerId());

        // Decrypt appId
        if (entity.getAppId() != null) {
            if (EncryptedValue.isEncrypted(entity.getAppId())) {
                try {
                    byte[] decrypted = cryptoProvider.decrypt(EncryptedValue.deserialize(entity.getAppId()));
                    dto.setAppId(new String(decrypted, StandardCharsets.UTF_8));
                } catch (Exception _) {
                    dto.setAppId(entity.getAppId());
                }
            } else {
                dto.setAppId(entity.getAppId());
            }
        }

        // Decrypt appSecret
        if (entity.getAppSecret() != null) {
            if (EncryptedValue.isEncrypted(entity.getAppSecret())) {
                try {
                    byte[] decrypted = cryptoProvider.decrypt(EncryptedValue.deserialize(entity.getAppSecret()));
                    dto.setAppSecret(new String(decrypted, StandardCharsets.UTF_8));
                } catch (Exception _) {
                    dto.setAppSecret(entity.getAppSecret());
                }
            } else {
                dto.setAppSecret(entity.getAppSecret());
            }
        }

        dto.setEnabled(entity.getEnabled());
        return dto;
    }
}
