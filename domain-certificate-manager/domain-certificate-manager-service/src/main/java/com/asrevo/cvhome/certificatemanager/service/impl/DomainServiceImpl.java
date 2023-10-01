package com.asrevo.cvhome.certificatemanager.service.Impl;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.certificatemanager.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainChangeReferenceRequest;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainChangeReferenceResponse;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.certificatemanager.domain.DomainEntity;
import com.asrevo.cvhome.certificatemanager.repository.DomainRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.domain.IdentityId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@AllArgsConstructor
@Slf4j
public class DomainServiceImpl implements DomainService {
    private final DomainRepository domainRepository;


    @Override
    public DomainEntity findOneByDomain(Domain domain) {
        return domainRepository.findByDomain(domain).orElse(null);
    }

    @Override
    public AvailabilityResponse checkAvailability(Domain domain) {
        return domainRepository.findByDomain(domain)
                .map(it -> new AvailabilityResponse(false))
                .orElseGet(() -> new AvailabilityResponse(true));
    }

    @Override
    public DomainReferenceResponse getReference(Domain domain) {
        return domainRepository.findByDomain(domain).map(it -> new DomainReferenceResponse(it.getReference())).orElseThrow(() -> new RuntimeException("couldn't find this domain"));
    }

    @Override
    public DomainEntity save(DomainEntity entity) {
        return domainRepository.save(entity);
    }

    @Transactional
    @Override
    public void updateDomainStatus(Domain domain, CertificateOrderStatus orderStatus, Instant date) {
        DomainEntity domainEntity = findOneByDomain(domain);
        switch (orderStatus) {
            case REQUESTED -> {
                if (domainEntity != null) {
                    DomainCertificateStatus newCertificateStatus = switch (domainEntity.getStatus()) {
                        case RENEWING_ORDER_PROCESS, EXPIRED_CERTIFICATE, EXPIRING_CERTIFICATE_SOON ->
                                DomainCertificateStatus.RENEWING_ORDER_PROCESS;
                        default -> DomainCertificateStatus.FIRST_ORDERING;
                    };
                    domainEntity.setStatus(newCertificateStatus);
                }
            }
            case GENERATED -> {
                domainEntity.setStatus(DomainCertificateStatus.ACTIVE_CERTIFICATE_GENERATED);
                domainEntity.setGeneratedDate(date);
            }
            case FAIL_GENERATING, PRE_VALIDATED_INVALID, VALIDATED_INVALID ->
                    domainEntity.setStatus(DomainCertificateStatus.FAILED_CERTIFICATE_GENERATING);
        }
        save(domainEntity);
    }

    @Transactional
    @Override
    public DomainChangeReferenceResponse changeDomainReference(DomainChangeReferenceRequest changeReferenceRequest, IdentityId identityId) {
        DomainEntity entity = domainRepository.findByDomain(changeReferenceRequest.domain()).orElseThrow(() -> new RuntimeException("this domain not registered yet"));
        if (!identityId.equals(entity.getOwner().getId())) {
            throw new RuntimeException("sorry you dont own this domain");
        }

        entity.changeDomainReference(changeReferenceRequest.reference());
        DomainEntity savedDomain = domainRepository.save(entity);
        return new DomainChangeReferenceResponse(savedDomain.getDomain(), savedDomain.getReference());

    }

}
