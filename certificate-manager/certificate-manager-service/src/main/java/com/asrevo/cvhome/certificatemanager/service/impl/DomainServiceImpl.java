package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.commons.command.order.CreateOrderCommand;
import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateId;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainCreateRequestDto;
import com.asrevo.cvhome.certificatemanager.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.mappers.DomainMappers;
import com.asrevo.cvhome.certificatemanager.repository.DomainRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.command.CommandPublisher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class DomainServiceImpl implements DomainService {
    private final CommandPublisher commandPublisher;
    private final DomainRepository domainRepository;
    private final DomainMappers domainMappers;

    @Transactional
    @Override
    public DomainEntity save(DomainEntity entity) {
        return domainRepository.save(entity);
    }

    @Override
    public DomainEntity findOneByDomain(Domain domain) {
        return domainRepository.findOneByDomain(domain);
    }


    @Override
    public DomainCreateResponseDto register(DomainCreateRequestDto createRequest) {
        return this.register(createRequest.getDomain(), createRequest.isAutoRenew(), createRequest.isAutoOrder());
    }

    @Override
    public DomainCreateResponseDto register(Domain domain, boolean autoRenew, boolean autoOrder) {
        DomainEntity entity = DomainEntity.createDomain(domain, autoRenew, autoOrder);
        DomainEntity savedDomain = save(entity);
        if (entity.isAutoOrder()) {
            CreateOrderCommand command = new CreateOrderCommand();
            command.setDomain(savedDomain.getDomain());
            commandPublisher.publish(command);
        }
        return domainMappers.toDomainCreateResponse(savedDomain);
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

    @Transactional
    @Override
    public void addNewCertificateToDomain(Domain domain, OrdersId ordersId, CertificateId certificateId) {
        DomainEntity domainEntity = findOneByDomain(domain);
        if (domainEntity != null) {
            domainEntity.addNewCertificate(ordersId, certificateId);
            save(domainEntity);
        }
    }

}
