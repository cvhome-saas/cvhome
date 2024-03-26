package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.dto.PodDto;
import com.asrevo.cvhome.manager.service.StorePodClient;
import com.asrevo.cvhome.s2s.utils.WebClientsUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@AllArgsConstructor
public class StorePodClientFactory {
    private final WebClient.Builder defaultWebMicroServiceBuilder;

    public StorePodClient createClient(PodDto podDto) {
        return WebClientsUtils.build(defaultWebMicroServiceBuilder, podDto.location(), StorePodClient.class);
    }
}
