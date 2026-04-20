package com.asrevo.cvhome.controlplane.pod.api;

import java.util.List;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.Pod;

import reactor.core.publisher.Mono;

@HttpExchange("/api/v1/pod")
public interface ExternalPodClient {

    @GetExchange("list")
    Mono<List<Pod>> listPods();

}
