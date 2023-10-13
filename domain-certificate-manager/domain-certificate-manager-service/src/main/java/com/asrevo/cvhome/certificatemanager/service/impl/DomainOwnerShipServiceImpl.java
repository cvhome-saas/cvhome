package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.commons.command.order.CreateOrderCommand;
import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Reference;
import com.asrevo.cvhome.certificatemanager.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.certificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.certificatemanager.commons.dto.RegisterDomainResponse;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.service.DomainOwnerShipService;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.command.CommandPublisher;
import com.asrevo.cvhome.commons.domain.IdentityId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class DomainOwnerShipServiceImpl implements DomainOwnerShipService {
    private final DomainService domainService;
    private final CommandPublisher commandPublisher;


    @Override
    public RegisterDomainResponse register(RegisterDomainRequest request, IdentityId owner) {
        AvailabilityResponse availability = domainService.checkAvailability(request.domain());
        if (availability.available()) {
            DomainEntity entity = doRegister(request.domain(), request.reference(), request.domainType(), owner);
            if (entity.getDomainType().equals(DomainType.SAS_CUSTOM)) {
                CreateOrderCommand command = new CreateOrderCommand();
                command.setDomain(request.domain());
                command.setValidationType(Optional.ofNullable(request.recommendedChallengeValidationType()).orElse(ChallengeValidationType.TlsAlpn01));
                commandPublisher.publish(command);
            }
            return new RegisterDomainResponse(entity.getDomain(), entity.getReference());
        }
        throw new RuntimeException("domain already registered");
    }

    @Override
    public void registerReservedDomainToSys(RegisterDomainRequest request) {
        AvailabilityResponse availability = domainService.checkAvailability(request.domain());
        if (availability.available()) {
            doRegister(request.domain(), request.reference(), DomainType.APPLICATION_RESERVED, IdentityId.ofSys());
            CreateOrderCommand command = new CreateOrderCommand();
            command.setDomain(request.domain());
            command.setValidationType(Optional.ofNullable(request.recommendedChallengeValidationType()).orElse(ChallengeValidationType.TlsAlpn01));
            commandPublisher.publish(command);
        }
        throw new RuntimeException("domain already registered");
    }

    private DomainEntity doRegister(Domain domain, Reference reference, DomainType domainType, IdentityId identity) {
        if (domainType.equals(DomainType.SAS_CUSTOM)) {
            boolean provedTo = domain.isProvedTo(identity);
            if (!provedTo) {
                throw new RuntimeException("please prove domain ownership on domain " + domain.domain() + " by adding txt record " + domain.getProvingDomain() + " with value " + identity.id());
            }
        }
        return domainService.save(DomainEntity.create(domain, reference, domainType, identity));
    }


}
