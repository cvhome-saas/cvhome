package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.domainownership.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.domainownership.domain.DomainEntity;

public interface DomainService {
    DomainEntity findOneByDomain(Domain domain);

    AvailabilityResponse checkAvailability(Domain domain);

    DomainReferenceResponse getReference(Domain domain);

    DomainEntity save(DomainEntity entity);

    void updateDomainStatus(Domain domain);
}
