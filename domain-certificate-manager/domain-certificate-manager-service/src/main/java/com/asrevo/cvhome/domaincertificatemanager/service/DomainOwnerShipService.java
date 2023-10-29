package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisterDomainRequest;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.RegisterDomainResponse;

public interface DomainOwnerShipService {
    RegisterDomainResponse register(RegisterDomainRequest request, IdentityId identityId);

    void registerReservedDomainToSys(RegisterDomainRequest request);
}
