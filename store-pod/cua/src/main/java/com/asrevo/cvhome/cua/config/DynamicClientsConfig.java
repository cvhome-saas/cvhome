package com.asrevo.cvhome.cua.config;

import com.asrevo.cvhome.cua.repo.SocialLoginConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import java.util.Map;

@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(OAuth2ClientProperties.class)
public class DynamicClientsConfig {

	private final SocialLoginConfigRepository socialLoginConfigRepository;

	@Bean
	@Lazy
	public DynamicClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
		Map<String, ClientRegistration> registrations = new OAuth2ClientPropertiesMapper(properties)
			.asClientRegistrations();
		return new DynamicClientRegistrationRepository(socialLoginConfigRepository, registrations);
	}

}
