package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.*;

import java.time.Instant;
import java.util.List;

public interface DomainService {
    DomainEntity findOneByDomain(Domain domain);

    AvailabilityResponse checkAvailability(Domain domain);

    DomainReferenceResponse getReference(Domain domain);

    DomainEntity save(DomainEntity entity);

    void updateDomainStatus(Domain domain, CertificateOrderStatus certificateOrderStatus, Instant date);

    DomainChangeReferenceResponse changeDomainReference(DomainChangeReferenceRequest changeReferenceRequest, IdentityId identityId);

    List<DomainEntity> findAllSystemDomainNotRequested();

    List<RegisteredDomainResponse> findAllRegisteredDomains(IdentityId identityId, DomainType domainType);
}
