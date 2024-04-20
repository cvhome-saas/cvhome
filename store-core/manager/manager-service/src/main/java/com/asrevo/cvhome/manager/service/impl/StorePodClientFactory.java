package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.dto.IpodDto;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class StorePodClientFactory {
    private final WebClient.Builder defaultWebMicroServiceBuilder;
    private final Map<PodId, StorePodClient> clientsMap = new HashMap<>();

    public StorePodClient createClient(IpodDto ipodDto) {
        return clientsMap.computeIfAbsent(ipodDto.id(), podId ->
                WebClientsUtils.build(defaultWebMicroServiceBuilder, ipodDto.location()+"/store", StorePodClient.class));
    }
}
