package com.asrevo.cvhome.dcm.jobs;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.dcm.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.dcm.entity.DomainEntity;
import com.asrevo.cvhome.dcm.service.DomainOwnerShipService;
import com.asrevo.cvhome.dcm.service.DomainService;
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
                RegisterDomainRequest request = new RegisterDomainRequest(it,
                        null,
                        null,
                        ChallengeValidationType.Dns01,
                        true);
                domainOwnerShipService.registerReservedDomainToSys(request);
            }
        });
    }
}
