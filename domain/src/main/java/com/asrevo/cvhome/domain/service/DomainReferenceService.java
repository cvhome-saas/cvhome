package com.asrevo.cvhome.domain.service;

import com.asrevo.cvhome.domain.domain.DomainReference;
import com.asrevo.cvhome.domain.domain.DomainType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DomainReferenceService {
    Mono<DomainReference> save(DomainReference domainReference, String reference);

    Mono<DomainReference> getDomainReference(String domain);

    Flux<DomainReference> getAllDomainReferences(String sub, DomainType domainType);

    Mono<DomainReference> findById(Long domainId);

    Mono<DomainReference> updateExternalAcmOrderId(Long domainId, Long externalAcmOrderId);
}
