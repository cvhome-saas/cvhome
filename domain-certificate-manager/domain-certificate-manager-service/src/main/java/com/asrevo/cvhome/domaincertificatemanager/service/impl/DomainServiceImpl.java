package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.*;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.domaincertificatemanager.mappers.DomainMappers;
import com.asrevo.cvhome.domaincertificatemanager.repository.DomainRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class DomainServiceImpl implements DomainService {
    private final DomainRepository domainRepository;
    private final DomainMappers domainMappers;


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

    @Override
    public List<DomainEntity> findAllSystemDomainNotRequested() {
        return domainRepository.findByOwnerAndStatusIs(AggregateReference.to(IdentityId.ofSys()), DomainCertificateStatus.INITIATED);
    }

    @Override
    public List<RegisteredDomainResponse> findAllRegisteredDomains(IdentityId identityId, DomainType domainType) {
        List<DomainType> domainTypes = Optional.ofNullable(domainType).map(List::of)
                .orElseGet(() ->
                        List.of(DomainType.SAS_CUSTOM, DomainType.SAS_SUB));
        AggregateReference<OwnerEntity, IdentityId> owner = AggregateReference.to(identityId);
        List<DomainEntity> domains = domainRepository.findByOwnerAndDomainTypeIn(owner, domainTypes);
        return domainMappers.toRegisteredDomainResponse(domains);
    }

}
