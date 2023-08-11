package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.domain.DomainReference;
import com.asrevo.cvhome.certificatemanager.repository.DomainReferenceRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainReferenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class DomainReferenceServiceImpl implements DomainReferenceService {
    private final DomainReferenceRepository domainReferenceRepository;

    @Override
    public DomainReference saveOrUpdate(DomainReference domainReference) {
        Optional<DomainReference> existingDomainReference = domainReferenceRepository.findOneByDomain(domainReference.domain());
        return existingDomainReference.map(it -> domainReferenceRepository.save(new DomainReference(it.id(), it.domain(), domainReference.reference()))).orElseGet(() -> domainReferenceRepository.save(domainReference));
    }

    @Override
    public Optional<DomainReference> getDomainReference(String domain) {
        return domainReferenceRepository.findOneByDomain(domain);
    }
}
