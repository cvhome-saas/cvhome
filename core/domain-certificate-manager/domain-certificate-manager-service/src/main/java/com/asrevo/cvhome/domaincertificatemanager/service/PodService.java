package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.PodId;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Reference;
import com.asrevo.cvhome.domaincertificatemanager.commons.dto.PodDto;

public interface PodService {
    PodId selectPod(Domain domain, Reference reference, DomainType domainType, IdentityId identity);

    PodDto getPod(PodId podId);
}
