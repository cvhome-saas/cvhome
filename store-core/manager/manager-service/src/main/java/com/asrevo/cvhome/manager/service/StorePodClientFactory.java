package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.merchant.api.StorePodClient;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorePodClientFactory {

	private final Map<PodId, StorePodClient> clients = new ConcurrentHashMap<>();

	private final ServiceDomainProperties serviceDomainProperties;

	private final WebClient.Builder defaultWebMicroServiceBuilder;

	private final Environment environment;

	public StorePodClient getClient(PodId podId) {
		return clients.computeIfAbsent(podId, this::create);
	}

	private StorePodClient create(PodId podId) {
		ProxyClient proxyClient = getProxyClient(podId);
		return new StorePodClientImpl(proxyClient);
	}

	private ProxyClient getProxyClient(PodId podId) {
		Pod pod = serviceDomainProperties.getPodByPodId(podId).get();
		String gatewayUrl = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(pod);
		return new ProxyClient(defaultWebMicroServiceBuilder.baseUrl(gatewayUrl).build());
	}

}
