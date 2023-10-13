package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.certificatemanager.commons.dto.RegisterDomainResponse;
import com.asrevo.cvhome.commons.domain.IdentityId;

public interface DomainOwnerShipService {
    RegisterDomainResponse register(RegisterDomainRequest request, IdentityId identityId);

    RegisterDomainResponse registerReservedDomainToSys(RegisterDomainRequest request);
}
