package com.asrevo.cvhome.gateway.config;

import com.asrevo.cvhome.controlplane.pod.api.ExternalPodClient;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientsConfig {

	@Bean
	public ExternalPodClient externalPodClient(WebClientBuilder webClientBuilder) {
		return webClientBuilder.buildClient("control-plane", ExternalPodClient.class);
	}

}
