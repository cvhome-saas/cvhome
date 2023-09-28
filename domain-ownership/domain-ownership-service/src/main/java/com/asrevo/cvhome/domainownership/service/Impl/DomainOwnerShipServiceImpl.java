package com.asrevo.cvhome.domainownership.service.Impl;

import com.asrevo.cvhome.certificatemanager.commons.command.domain.CreateDomainCommand;
import com.asrevo.cvhome.commons.command.CommandPublisher;
import com.asrevo.cvhome.domainownership.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.domain.Reference;
import com.asrevo.cvhome.domainownership.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.domainownership.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainResponse;
import com.asrevo.cvhome.domainownership.domain.DomainEntity;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;
import com.asrevo.cvhome.domainownership.repository.DomainRepository;
import com.asrevo.cvhome.domainownership.service.DomainOwnerShipService;
import com.asrevo.cvhome.domainownership.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@AllArgsConstructor
@Service
public class DomainOwnerShipServiceImpl implements DomainOwnerShipService {
    private final DomainRepository domainRepository;
    private final OwnerService ownerService;
    private final CommandPublisher commandPublisher;


    @Override
    public RegisterDomainResponse register(RegisterDomainRequest request, Principal principal) {
        AvailabilityResponse availability = checkAvailability(request.domain());
        if (availability.available()) {
            DomainEntity entity = switch (request.domain().domainType()) {
                case APPLICATION_RESERVED -> doSystemRegister(request.domain(), request.reference());
                default -> doClientRegister(principal, request.domain(), request.reference());
            };
            CreateDomainCommand command = new CreateDomainCommand();
            command.setDomain(new com.asrevo.cvhome.certificatemanager.commons.domain.Domain(request.domain().domain()));
            command.setAutoRenew(true);
            command.setAutoOrder(true);
            commandPublisher.publish(command);
            return new RegisterDomainResponse(entity.getDomain(), entity.getReference());
        }
        throw new RuntimeException("domain already registered");
    }

    @Override
    public DomainReferenceResponse getReference(Domain domain) {
        return domainRepository.findByDomain(domain).map(it -> new DomainReferenceResponse(it.getReference())).orElseThrow(() -> new RuntimeException("couldn't find this domain"));
    }

    private DomainEntity doClientRegister(Principal principal, Domain domain, Reference reference) {
        OwnerEntity owner = ownerService.getOwner(principal);
        if (domain.isProvedTo(owner.getIdentity())) {
            DomainEntity entity = owner.addDomain(domain, reference);
            ownerService.save(owner);
            return entity;
        } else {
            throw new RuntimeException("please prove domain ownership on domain " + domain.domain() + " by adding txt record " + domain.getProvingDomain() + " with value " + owner.getIdentity().identity());
        }
    }

    private DomainEntity doSystemRegister(Domain domain, Reference reference) {
        OwnerEntity owner = ownerService.getSystemOwner();
        DomainEntity entity = owner.addDomain(domain, reference);
        ownerService.save(owner);
        return entity;
    }

    @Override
    public AvailabilityResponse checkAvailability(Domain domain) {
        return domainRepository.findByDomain(domain)
                .map(it -> new AvailabilityResponse(false))
                .orElseGet(() -> new AvailabilityResponse(true));
    }

}
