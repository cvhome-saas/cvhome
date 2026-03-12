package com.asrevo.cvhome.uaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		// Delegating encoder supports multiple encodings and prefixes, defaults to
		// {bcrypt}
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

}
