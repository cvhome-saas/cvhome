package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.s2s.config.internal.ServiceUrlBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

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

	private final Map<PodId, ProxyClient> clients = new ConcurrentHashMap<>();

	private final ServiceDomainProperties serviceDomainProperties;

	private final WebClient.Builder defaultWebMicroServiceBuilder;

	private final Environment environment;

	public MerchantStorePodClient getMerchantStorePodClient(PodId podId) {
		ProxyClient proxy = clients.computeIfAbsent(podId, this::createPodProxy);
		return new MerchantMerchantStorePodClientImpl(proxy);
	}

	private ProxyClient createPodProxy(PodId podId) {
		// @TODO check if private or public pod and a way to resolve .get
		Pod pod = serviceDomainProperties.getPodByPodId(podId).get();
		String podGateway = new ServiceUrlBuilder(serviceDomainProperties, environment).getServiceUrl(pod);
		return new ProxyClient(defaultWebMicroServiceBuilder.baseUrl(podGateway).build());
	}

}
