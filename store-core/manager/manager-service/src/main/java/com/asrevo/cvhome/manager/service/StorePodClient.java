package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1")
public interface StorePodClient {
    @PostExchange("create")
    CreateStoreResponse create(@RequestBody CreateStoreResponse dto);

    @GetExchange("private/store/{code}")
    Mono<Object> getStore(@PathVariable("code") String code);
}
