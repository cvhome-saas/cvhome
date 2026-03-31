package com.asrevo.cvhome.cua.config;

import com.asrevo.cvhome.cua.domain.SocialLoginConfig;
import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.util.StringUtils;

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

		int lastDotIndex = registrationId.lastIndexOf(".");
		String clientId = registrationId.substring(0, lastDotIndex);
		String providerId = registrationId.substring(lastDotIndex + 1);

		return socialLoginConfigRepository.findByClientIdAndProviderId(clientId, providerId)
			.map(config -> createClientRegistration(registrationId, config))
			.orElse(null);
	}

	private ClientRegistration.Builder createBuilder(String registrationId, SocialLoginConfig config) {
		SocialProvider socialProvider = SocialProvider.fromProviderId(config.getProviderId());
		if (socialProvider != null) {
			return socialProvider.createBuilder(registrationId);
		}

		throw new IllegalArgumentException("Unknown social provider: " + config.getProviderId());
	}

	private ClientRegistration createClientRegistration(String registrationId, SocialLoginConfig config) {
		SocialProvider socialProvider = SocialProvider.fromProviderId(config.getProviderId());
		ClientRegistration.Builder builder = createBuilder(registrationId, config);

		builder.clientId(config.getAppId()).clientSecret(config.getAppSecret());

		if (socialProvider != null) {
			builder.clientName(socialProvider.getClientName());
		}
		else {
			builder.clientName(config.getProviderId());
		}

		if (StringUtils.hasText(config.getScopes())) {
			builder.scope(config.getScopes().split(","));
		}

		return builder.build();
	}

}
