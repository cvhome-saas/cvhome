package com.asrevo.cvhome.s2s.config.internal;

import static com.asrevo.cvhome.s2s.utils.WebClientsUtils.build;

import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class WebClientBuilder {

	private final Environment environment;

	private final WebClient.Builder defaultMicroServiceBuilder;

	private final ServiceDomainProperties serviceDomainProperties;

	private final ObjectMapper objectMapper;

	public WebClientBuilder(Environment environment, WebClient.Builder defaultMicroServiceBuilder,
			ServiceDomainProperties serviceDomainProperties, ObjectMapper objectMapper) {
		this.environment = environment;
		this.defaultMicroServiceBuilder = defaultMicroServiceBuilder;
		this.serviceDomainProperties = serviceDomainProperties;
		this.objectMapper = objectMapper;
	}

	public <T> T buildClient(String serviceName, Class<T> tClass) {
		String url = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(serviceName);
		return build(defaultMicroServiceBuilder, url, tClass, objectMapper);
	}

	public <T> T buildClient(String serviceName, Class<T> tClass, ObjectMapper objectMapper) {
		String url = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(serviceName);
		return build(defaultMicroServiceBuilder, url, tClass, objectMapper);
	}

}
