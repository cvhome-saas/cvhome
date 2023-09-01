package com.asrevo.cvhome.domain.service.Impl;

import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.domain.DomainStatus;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.domain.repository.DomainReferenceRepository;
import com.asrevo.cvhome.domain.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@AllArgsConstructor
public class DomainReferenceServiceImpl implements DomainReferenceService {
    private final DomainReferenceRepository domainReferenceRepository;

    @Override
    public Mono<DomainReference> save(DomainReference d, String reference) {
        return domainReferenceRepository.save(new DomainReference(null, d.domain(), reference, d.domainType(), DomainStatus.INITIATED, Instant.now(), null));
    }

    @Override
    public Mono<DomainReference> getDomainReference(String domain) {
        return domainReferenceRepository.findOneByDomain(domain);
    }

    @Override
    public Flux<DomainReference> getAllDomainReferences(String sub, DomainType domainType) {
        return domainReferenceRepository.getAllByReferenceAndDomainType(sub, domainType);
    }

    @Override
    public Mono<DomainReference> findById(Long domainId) {
        return domainReferenceRepository.findById(domainId);
    }

    @Transactional
    public Mono<DomainReference> updateExternalAcmOrderId(Long domainId, Long externalAcmOrderId) {
        return domainReferenceRepository.updateExternalAcmOrderId(domainId, externalAcmOrderId)
                .flatMap(it -> domainReferenceRepository.findById(domainId));
    }
}
