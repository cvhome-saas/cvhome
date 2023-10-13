package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Reference;

public record DomainChangeReferenceRequest(Domain domain, Reference reference) {
}
