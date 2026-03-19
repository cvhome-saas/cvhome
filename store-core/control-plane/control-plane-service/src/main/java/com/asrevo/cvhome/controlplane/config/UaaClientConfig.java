package com.asrevo.cvhome.controlplane.config;

import com.asrevo.cvhome.uaa.sdk.AdminUserClient;
import com.asrevo.cvhome.uaa.service.UserAccountService;
import com.asrevo.cvhome.uaa.service.impl.UserAccountServiceImpl;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UaaClientConfig.UaaClientConfigProperties.class)
public class UaaClientConfig {

	@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.client")
	public record UaaClientConfigProperties(String baseUrl, String clientId, String clientSecret) {
	}

	@SneakyThrows
	@Bean
	public UserAccountService userAccountService(UaaClientConfigProperties properties) {
		var client = new AdminUserClient(properties.baseUrl(), properties.clientId(), properties.clientSecret());
		return new UserAccountServiceImpl(client);
	}

}
