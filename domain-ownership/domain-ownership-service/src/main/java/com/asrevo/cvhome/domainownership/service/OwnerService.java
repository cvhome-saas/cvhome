package com.asrevo.cvhome.domainownership.service;

import com.asrevo.cvhome.domainownership.domain.OwnerEntity;

import java.security.Principal;

public interface OwnerService {
    OwnerEntity getSystemOwner();

    OwnerEntity getOwner(Principal principal);

    OwnerEntity save(OwnerEntity owner);
}
