package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.certificatemanager.domain.OwnerEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;

import java.security.Principal;

public interface OwnerService {
    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);

    void addDomain(IdentityId identityId, DomainId domainId);
}
