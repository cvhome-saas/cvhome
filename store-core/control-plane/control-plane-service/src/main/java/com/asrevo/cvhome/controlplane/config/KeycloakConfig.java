package com.asrevo.cvhome.controlplane.config;

import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.keycloak.service.impl.KeycloakUserAccountServiceImpl;
import com.asrevo.cvhome.s2s.model.KeycloakCredentialsProperties;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

	@SneakyThrows
	@Bean
	public UserAccountService userAccountService(KeycloakCredentialsProperties properties) {
		return new KeycloakUserAccountServiceImpl(properties.credentials());
	}

}
