package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.commons.command.CommandPublisher;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.domaincertificatemanager.commons.command.order.CreateOrderCommand;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisterDomainResponse;
import com.asrevo.cvhome.domaincertificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainOwnerShipService;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Record;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
@Service
public class DomainOwnerShipServiceImpl implements DomainOwnerShipService {
    private final DomainService domainService;
    private final CommandPublisher commandPublisher;
    private final AutoOrderDomainsProperties autoOrderDomainsProperties;


    @Override
    public RegisterDomainResponse register(RegisterDomainRequest request, IdentityId owner) {
        AvailabilityResponse availability = domainService.checkAvailability(request.domain());
        if (availability.available()) {
            DomainEntity entity = doRegister(request.domain(), request.reference(), request.domainType(), request.includeSubDomains(), owner);
            if (entity.getDomainType().equals(DomainType.SAS_CUSTOM)) {
                CreateOrderCommand command = new CreateOrderCommand();
                command.setDomain(request.domain());
                command.setIncludeSubDomains(request.includeSubDomains());
                command.setValidationType(Optional.ofNullable(request.recommendedType()).orElse(ChallengeValidationType.TlsAlpn01));
                commandPublisher.publish(command);
            }
            return new RegisterDomainResponse(entity.getDomain(), entity.getReference());
        }
        throw new RuntimeException("domain already registered");
    }

    @Override
    public void registerReservedDomainToSys(RegisterDomainRequest request) {
        if (request.includeSubDomains() && !ChallengeValidationType.Dns01.equals(request.recommendedType())) {
            throw new RuntimeException("can't request order with all sub domains when validation is not dns");
        }

        AvailabilityResponse availability = domainService.checkAvailability(request.domain());
        if (availability.available()) {
            doRegister(request.domain(), request.reference(), DomainType.APPLICATION_RESERVED, request.includeSubDomains(), IdentityId.ofSys());
            CreateOrderCommand command = new CreateOrderCommand();
            command.setIncludeSubDomains(request.includeSubDomains());
            command.setDomain(request.domain());
            command.setValidationType(Optional.ofNullable(request.recommendedType()).orElse(ChallengeValidationType.TlsAlpn01));
            commandPublisher.publish(command);
        } else {
            throw new RuntimeException("domain already registered");
        }
    }

    private DomainEntity doRegister(Domain domain, Reference reference, DomainType domainType, boolean includeSubDomains, IdentityId identity) {
        if (domainType.equals(DomainType.SAS_CUSTOM)) {
            boolean txtProvedTo = isTXTProvedTo(identity, domain.getProvingDomain());
            if (!txtProvedTo) {
                throw new RuntimeException("please prove domain ownership on domain " + domain.domain() + " by adding txt record " + domain.getProvingDomain() + " with value " + identity.id());
            }
            boolean cnameProvedTo = isCNAMEProvedTo(domain.domain(), autoOrderDomainsProperties.getDefaultDomain());
            if (!cnameProvedTo) {
                throw new RuntimeException("please prove domain ownership on domain " + domain.domain() + " by adding txt record " + domain.getProvingDomain() + " with value " + identity.id());
            }
        }
        return domainService.save(DomainEntity.create(domain, reference, domainType, includeSubDomains, identity));
    }

    private boolean isTXTProvedTo(IdentityId identityId, String provingDomain) {
        try {
            final Lookup lookup = new Lookup(provingDomain, Type.TXT);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0) {
                return Arrays.stream(records).flatMap(it -> {
                            if (it.getType() == 16) {
                                TXTRecord txtRecord = (TXTRecord) it;
                                return txtRecord.getStrings().stream();
                            } else {
                                return Stream.of();
                            }
                        })
                        .anyMatch(it -> it.equals(identityId.id()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean isCNAMEProvedTo(String lookupDomain, Domain domain) {
        try {
            final Lookup lookup = new Lookup(lookupDomain, Type.CNAME);
            lookup.setResolver(new SimpleResolver());
            lookup.setCache(null);
            Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL && records != null && records.length > 0) {
                return Arrays.stream(records)
                        .filter(it -> it.getType() == Type.CNAME)
                        .map(it -> ((CNAMERecord) it))
                        .map(it -> it.getTarget().toString())
                        .filter(it -> it != null && !it.isEmpty())
                        .anyMatch(it -> it.substring(0, it.length() - 1).equals(domain.domain()));
            }
        } catch (Exception ignored) {
        }
        return false;
    }

}
