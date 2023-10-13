package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainId;
import com.asrevo.cvhome.domaincertificatemanager.entity.OwnerEntity;
import com.asrevo.cvhome.commons.domain.IdentityId;

import java.security.Principal;

public interface OwnerService {
    OwnerEntity getOwnerOrCreate(IdentityId identityId, Principal principal);

    void addDomain(IdentityId identityId, DomainId domainId);
}
