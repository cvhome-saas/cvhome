package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.domainownership.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.dto.AvailabilityResponse;
import com.asrevo.cvhome.domainownership.commons.dto.DomainReferenceResponse;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainResponse;

import java.security.Principal;

public interface DomainOwnerShipService {
    AvailabilityResponse checkAvailability(Domain domain);

    RegisterDomainResponse register(RegisterDomainRequest request, Principal identityId);

    DomainReferenceResponse getReference(Domain domain);
}
