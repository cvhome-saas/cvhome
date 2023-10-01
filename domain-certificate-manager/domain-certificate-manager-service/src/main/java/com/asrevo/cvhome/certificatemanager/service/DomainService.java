package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainChangeReferenceRequest;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainChangeReferenceResponse;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.certificatemanager.domain.DomainEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;

import java.time.Instant;

public interface DomainService {
    DomainEntity findOneByDomain(Domain domain);

    AvailabilityResponse checkAvailability(Domain domain);

    DomainReferenceResponse getReference(Domain domain);

    DomainEntity save(DomainEntity entity);

    void updateDomainStatus(Domain domain, CertificateOrderStatus certificateOrderStatus, Instant date);

    DomainChangeReferenceResponse changeDomainReference(DomainChangeReferenceRequest changeReferenceRequest, IdentityId identityId);
}
