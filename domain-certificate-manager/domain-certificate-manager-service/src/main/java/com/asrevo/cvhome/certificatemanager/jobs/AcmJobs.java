package com.asrevo.cvhome.certificatemanager.jobs;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.certificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.certificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.service.DomainOwnerShipService;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.domain.IdentityId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class AcmJobs {
    private final DomainService domainService;
    private final AutoOrderDomainsProperties properties;
    private final DomainOwnerShipService domainOwnerShipService;

    public void orderSystemDomains() {
        List<Domain> domains = properties.getDomains();
        domains.forEach(it -> {
            DomainEntity entity = domainService.findOneByDomain(it);
            if (entity == null) {
                domainOwnerShipService.register(new RegisterDomainRequest(it, DomainType.APPLICATION_RESERVED, null, ChallengeValidationType.Dns01), IdentityId.ofSys());
            }
        });
    }
}
