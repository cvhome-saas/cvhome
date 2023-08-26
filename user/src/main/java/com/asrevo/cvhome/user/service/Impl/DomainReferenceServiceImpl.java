package com.asrevo.cvhome.user.service.Impl;

import com.asrevo.cvhome.user.domain.DomainReference;
import com.asrevo.cvhome.user.domain.DomainStatus;
import com.asrevo.cvhome.user.domain.DomainType;
import com.asrevo.cvhome.user.repository.DomainReferenceRepository;
import com.asrevo.cvhome.user.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        return domainReferenceRepository.save(new DomainReference(null, d.domain(), reference, d.domainType(), DomainStatus.INITIATED, Instant.now()));
    }

    @Override
    public Mono<DomainReference> getDomainReference(String domain) {
        return domainReferenceRepository.findOneByDomain(domain);
    }

    @Override
    public Flux<DomainReference> getAllDomainReferences(String sub, DomainType domainType) {
        return domainReferenceRepository.getAllByReferenceAndDomainType(sub, domainType);
    }
}
