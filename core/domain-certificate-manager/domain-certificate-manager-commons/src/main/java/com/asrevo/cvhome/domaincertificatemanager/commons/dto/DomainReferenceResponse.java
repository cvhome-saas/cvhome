package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.commons.domain.DomainStatus;
import com.asrevo.cvhome.commons.domain.Reference;

public record DomainReferenceResponse(DomainStatus domainStatus, Reference reference) {
}
