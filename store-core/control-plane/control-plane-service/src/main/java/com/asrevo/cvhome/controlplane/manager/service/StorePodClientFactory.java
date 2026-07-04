package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorePodClientFactory {

    private final Map<PodId, MerchantStorePodClient> clients = new ConcurrentHashMap<>();

    private final ServiceDomainProperties serviceDomainProperties;

    private final RestClientBuilder restClientBuilder;

    public MerchantStorePodClient getMerchantStorePodClient(PodId podId) {
        return clients.computeIfAbsent(podId, this::createMerchantStorePodClient);
    }

    private MerchantStorePodClient createMerchantStorePodClient(PodId podId) {
        Pod pod = serviceDomainProperties.getPodByPodId(podId)
                .orElseThrow(() -> new IllegalArgumentException("Pod not found for id: " + podId));
        return restClientBuilder.buildClient(pod, "merchant", MerchantStorePodClient.class);
    }

}
