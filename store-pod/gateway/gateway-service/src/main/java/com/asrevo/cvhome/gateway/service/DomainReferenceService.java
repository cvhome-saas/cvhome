package com.asrevo.cvhome.gateway.service;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.gateway.models.DomainReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

@HttpExchange("api/v1/domain-ownership")
public interface DomainReferenceService {
    @PostExchange("/get-reference")
    Mono<ResponseEntity<DomainReference>> getReference(@RequestBody Domain domain);
}
