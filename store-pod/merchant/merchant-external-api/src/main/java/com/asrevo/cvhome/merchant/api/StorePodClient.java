package com.asrevo.cvhome.merchant.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1")
public interface StorePodClient {
    @PostExchange("/private/store")
    Mono<ResponseEntity<Void>> create(@RequestBody Map<Object, Object> dto);

    @GetExchange("private/store")
    Mono<Map<String, Object>> getStore(@RequestParam("store") String store);
}
