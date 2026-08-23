package com.asrevo.cvhome.tenancy.manager.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.errors.remote.RemoteErrorCatalog;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.s2s.config.internal.RestClientBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class StorePodClientFactory {

    private final Map<PodId, MerchantStorePodClient> clients = new ConcurrentHashMap<>();

    private final CachingPodDirectory podDirectory;

    private final RestClientBuilder restClientBuilder;

    public MerchantStorePodClient getMerchantStorePodClient(PodId podId) {
        return clients.computeIfAbsent(podId, this::createMerchantStorePodClient);
    }

    private MerchantStorePodClient createMerchantStorePodClient(PodId podId) {
        // The directory, not the configuration seed: placement may pick a pod an operator registered through the
        // API, which configuration has never heard of. A miss here fails this outbox attempt only — the directory
        // refreshes within its TTL and the retry resolves it.
        Pod pod = podDirectory.find(podId)
                .orElseThrow(() -> new IllegalArgumentException("Pod not found for id: %s".formatted(podId)));
        return restClientBuilder.buildClient(pod, "merchant", MerchantStorePodClient.class, RemoteErrorCatalog.none());
    }

}
