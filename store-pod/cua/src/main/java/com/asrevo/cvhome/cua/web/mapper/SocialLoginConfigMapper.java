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
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialLoginConfigMapper {

    private final SecretCryptoProvider cryptoProvider;

    public SocialLoginConfig toEntity(PersistableSocialLoginConfig dto) {
        if (dto == null) {
            return null;
        }

        SocialLoginConfig entity = new SocialLoginConfig();
        entity.setId(new SocialLoginConfigId(dto.getStoreMerchantId(), dto.getProviderId()));

        // Encrypt appId
        if (dto.getAppId() != null && !EncryptedValue.isEncrypted(dto.getAppId())) {
            EncryptedValue encrypted = cryptoProvider.encrypt(dto.getAppId().getBytes(StandardCharsets.UTF_8));
            entity.setAppId(encrypted.serialize());
        }

        // Encrypt appSecret
        if (dto.getAppSecret() != null && !EncryptedValue.isEncrypted(dto.getAppSecret())) {
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
        if (EncryptedValue.isEncrypted(entity.getAppId())) {
            try {
                byte[] decrypted = cryptoProvider.decrypt(EncryptedValue.deserialize(entity.getAppId()));
                dto.setAppId(new String(decrypted, StandardCharsets.UTF_8));
            } catch (Exception _) {
                log.error("Failed to decrypt appId for social login config with id: {}", entity.getId());
            }
        }

        // Decrypt appSecret
        if (EncryptedValue.isEncrypted(entity.getAppSecret())) {
            try {
                byte[] decrypted = cryptoProvider.decrypt(EncryptedValue.deserialize(entity.getAppSecret()));
                dto.setAppSecret(new String(decrypted, StandardCharsets.UTF_8));
            } catch (Exception _) {
                log.error("Failed to decrypt appSecret for social login config with id: {}", entity.getId());
            }
        }
        return dto;
    }
}
