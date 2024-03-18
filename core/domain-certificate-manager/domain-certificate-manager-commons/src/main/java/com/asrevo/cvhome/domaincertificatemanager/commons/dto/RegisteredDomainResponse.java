package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.Reference;

public record RegisteredDomainResponse(Domain domain, DomainType domainType, Reference reference,
                                       DomainCertificateStatus status) {

}