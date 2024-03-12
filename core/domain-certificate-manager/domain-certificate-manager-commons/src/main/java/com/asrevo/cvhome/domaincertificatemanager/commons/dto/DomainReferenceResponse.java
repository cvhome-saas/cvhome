package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Reference;

public record DomainReferenceResponse(DomainStatus domainStatus, Reference reference, PodDto podDto) {
}
