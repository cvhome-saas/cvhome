package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainReference;

import java.util.Optional;

public interface DomainReferenceService {
    DomainReference saveOrUpdate(DomainReference domainReference);

    Optional<DomainReference> getDomainReference(String domain);
}
