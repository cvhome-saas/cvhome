package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domainownership.domain.OwnerEntity;

import java.security.Principal;

public interface OwnerService {
    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);

    void addDomain(IdentityId identityId, DomainId domainId);
}
