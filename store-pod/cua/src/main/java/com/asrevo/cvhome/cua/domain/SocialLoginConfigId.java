package com.asrevo.cvhome.cua.domain;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.config.SocialProvider;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;

public record SocialLoginConfigId(@Embedded StoreMerchantId storeMerchantId,
		@Enumerated(EnumType.STRING) SocialProvider providerId) implements Serializable {

	public String toRegistrationId() {
		return storeMerchantId.getId() + "." + providerId.name().toLowerCase();
	}

	public static SocialLoginConfigId fromRegistrationId(String registrationId) {
		int lastDotIndex = registrationId.lastIndexOf(".");
		StoreMerchantId storeMerchantId = new StoreMerchantId(registrationId.substring(0, lastDotIndex));
		SocialProvider provider = SocialProvider.valueOf(registrationId.substring(lastDotIndex + 1).toUpperCase());
		return new SocialLoginConfigId(storeMerchantId, provider);
	}
}
