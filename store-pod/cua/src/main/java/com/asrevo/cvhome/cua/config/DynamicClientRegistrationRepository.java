package com.asrevo.cvhome.cua.config;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import com.asrevo.cvhome.cua.domain.SocialLoginConfigId;
import com.asrevo.cvhome.cua.service.SocialLoginConfigService;
import com.asrevo.cvhome.cua.web.dto.ReadableSocialLoginConfig;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DynamicClientRegistrationRepository implements ClientRegistrationRepository {

    private final SocialLoginConfigService socialLoginConfigService;

    private final InMemoryClientRegistrationRepository delegate;

    public DynamicClientRegistrationRepository(SocialLoginConfigService socialLoginConfigService,
                                               Map<String, ClientRegistration> registrations) {

        this.socialLoginConfigService = socialLoginConfigService;
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

        return socialLoginConfigService.findById(SocialLoginConfigId.fromRegistrationId(registrationId))
                .map(config -> createClientRegistration(registrationId, config))
                .orElse(null);
    }

    private ClientRegistration createClientRegistration(String registrationId, ReadableSocialLoginConfig config) {

        ClientRegistration.Builder builder = createBuilder(registrationId, config);

        builder.clientId(config.getAppId()).clientSecret(config.getAppSecret());

        if (config.getProviderId() != null) {
            builder.clientName(config.getProviderId().getClientName());
        }

        return builder.build();
    }

    private ClientRegistration.Builder createBuilder(String registrationId, ReadableSocialLoginConfig config) {
        return config.getProviderId().createBuilder(registrationId);
    }


}
