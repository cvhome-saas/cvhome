package com.asrevo.cvhome.domainownership.service.Impl;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.domainownership.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.domainownership.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.domainownership.domain.DomainEntity;
import com.asrevo.cvhome.domainownership.repository.DomainRepository;
import com.asrevo.cvhome.domainownership.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void updateDomainStatus(Domain domain) {
        DomainEntity domainEntity = findOneByDomain(domain);
        if (domainEntity != null) {
            DomainCertificateStatus newCertificateStatus = switch (domainEntity.getStatus()) {
                case RENEWING_ORDER_PROCESS, EXPIRED_CERTIFICATE -> DomainCertificateStatus.RENEWING_ORDER_PROCESS;
                default -> DomainCertificateStatus.FIRST_ORDERING;
            };
            domainEntity.setStatus(newCertificateStatus);
            save(domainEntity);
        }
    }

}
