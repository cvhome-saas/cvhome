package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domainownership.commons.dto.RegisterDomainResponse;

public interface DomainOwnerShipService {
    RegisterDomainResponse register(RegisterDomainRequest request, IdentityId identityId);
}
