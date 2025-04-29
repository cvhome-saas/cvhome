package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.config.internal.WebClientBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import com.asrevo.cvhome.merchant.api.StorePodClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorePodClientFactory {
    private final Map<PodId, StorePodClient> clients = new ConcurrentHashMap<>();
    private final WebClientBuilder webClientBuilder;
    private final ServiceDomainProperties serviceDomainProperties;

    public StorePodClient getClient(PodId podId) {
        return clients.computeIfAbsent(podId, this::create);
    }

    private StorePodClient create(PodId podId) {
        // @TODO check if private or public pod and a way to resolve .get
        Pod pod = serviceDomainProperties.getPodByPodId(podId).get();

        ServiceDomain requestedService = serviceDomainProperties.getService("merchant");

        ServiceDomain gateway = serviceDomainProperties.getService(requestedService.gatewayServiceName());
        return  webClientBuilder.buildInternalClient(
                gateway.name() + "." + pod.endpoint().endpoint() + "/" + "merchant", StorePodClient.class);

    }
}
