package com.asrevo.cvhome.domain.service;

import com.asrevo.cvhome.commons.domain.DomainReferenceOrder;
import com.asrevo.cvhome.commons.domain.DomainType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DomainReferenceAcmService {
    Mono<DomainReferenceOrder> order(Long domainId);

    Flux<DomainReferenceOrder> getAllDomainReferences(String sub, DomainType domainType);
}
