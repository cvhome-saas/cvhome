package com.asrevo.cvhome.gateway.service;

import com.asrevo.cvhome.gateway.models.DomainReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("api/domain-reference")
public interface DomainReferenceService {
    @GetExchange("/get-domain-reference")
    Mono<DomainReference> getDomainReference(@RequestParam(name = "domain") String domain);
}
