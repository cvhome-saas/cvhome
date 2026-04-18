package com.asrevo.cvhome.cua.domain;

import java.io.Serializable;

import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;

public record SocialLoginConfigId(@Embedded StoreMerchantId storeMerchantId,
                                  @Enumerated(EnumType.STRING) SocialProvider providerId) implements Serializable {
    private static final String PROVIDER_SPLITTER = ".";

    public static SocialLoginConfigId fromRegistrationId(String registrationId) {
        int lastDotIndex = registrationId.lastIndexOf(PROVIDER_SPLITTER);
        StoreMerchantId storeMerchantId = new StoreMerchantId(registrationId.substring(0, lastDotIndex));
        SocialProvider provider = SocialProvider.valueOf(registrationId.substring(lastDotIndex + 1).toUpperCase());
        return new SocialLoginConfigId(storeMerchantId, provider);
    }

    public String toRegistrationId() {
        return storeMerchantId.getId() + PROVIDER_SPLITTER + providerId.name().toLowerCase();
    }
}
