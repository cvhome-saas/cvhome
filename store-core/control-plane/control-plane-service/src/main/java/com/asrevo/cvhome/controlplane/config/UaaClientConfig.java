package com.asrevo.cvhome.controlplane.config;

import com.asrevo.cvhome.uaa.service.UserAccountService;
import com.asrevo.cvhome.uaa.service.impl.UserAccountServiceImpl;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UaaClientConfig {

	@SneakyThrows
	@Bean
	public UserAccountService userAccountService() {
		return new UserAccountServiceImpl();
	}

}
