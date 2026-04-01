package com.asrevo.cvhome.cua.config;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import java.util.*;

@RequiredArgsConstructor
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

	private final SocialLoginConfigRepository socialLoginConfigRepository;

	private final InMemoryClientRegistrationRepository delegate;

	public DynamicClientRegistrationRepository(SocialLoginConfigRepository socialLoginConfigRepository,
			Map<String, ClientRegistration> registrations) {

		this.socialLoginConfigRepository = socialLoginConfigRepository;
		this.delegate = new InMemoryClientRegistrationRepository(new ArrayList<>(registrations.values()));

	}

	@Override
	public ClientRegistration findByRegistrationId(String registrationId) {
		// First try the delegate (YAML registrations)
		ClientRegistration registration = delegate.findByRegistrationId(registrationId);
		if (registration != null) {
			return registration;
		}

		// Expect registrationId in the format: {clientId}.{providerId} for dynamic ones
		if (!registrationId.contains(".")) {
			return null;
		}

		return socialLoginConfigRepository.findById(SocialLoginConfigId.fromRegistrationId(registrationId))
			.map(config -> createClientRegistration(registrationId, config))
			.orElse(null);
	}

	private ClientRegistration.Builder createBuilder(String registrationId, SocialLoginConfig config) {
		return config.getId().providerId().createBuilder(registrationId);
	}

	private ClientRegistration createClientRegistration(String registrationId, SocialLoginConfig config) {

		ClientRegistration.Builder builder = createBuilder(registrationId, config);

		builder.clientId(config.getAppId()).clientSecret(config.getAppSecret());

		if (config.getId().providerId() != null) {
			builder.clientName(config.getId().providerId().getClientName());
		}

		return builder.build();
	}

}
