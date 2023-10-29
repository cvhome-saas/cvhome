package com.asrevo.cvhome.domaincertificatemanager.jobs;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domaincertificatemanager.config.AutoOrderDomainsProperties;
import com.asrevo.cvhome.domaincertificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainOwnerShipService;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
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
                        ChallengeValidationType.TlsAlpn01,
                        false);
                domainOwnerShipService.registerReservedDomainToSys(request);
            }
        });
    }
}
