package com.asrevo.cvhome.dcm.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.dcm.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.dcm.commons.dto.RegisterDomainResponse;

public interface DomainOwnerShipService {
    RegisterDomainResponse register(RegisterDomainRequest request, IdentityId identityId);

    void registerReservedDomainToSys(RegisterDomainRequest request);
}
