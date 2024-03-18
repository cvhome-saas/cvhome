package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;

import java.security.Principal;

public interface OwnerService {
    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);

    void addDomain(IdentityId identityId, DomainId domainId);
}
