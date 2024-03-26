package com.asrevo.cvhome.dcm.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.dcm.commons.domain.DomainCertificateStatus;

public record RegisteredDomainResponse(Domain domain, DomainType domainType, Reference reference,
                                       DomainCertificateStatus status) {

}