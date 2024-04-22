package com.asrevo.cvhome.manager.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@HttpExchange("/api/v1")
public interface StorePodClient {
    @PostExchange("/private/store")
    Mono<ResponseEntity<Void>> create(@RequestBody Map<Object, Object> dto);

    @GetExchange("private/store/{code}")
    Mono<Object> getStore(@PathVariable("code") String code);
}
