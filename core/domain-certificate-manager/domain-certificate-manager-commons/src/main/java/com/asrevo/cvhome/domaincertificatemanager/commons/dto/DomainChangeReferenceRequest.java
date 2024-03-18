package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;

public record DomainChangeReferenceRequest(Domain domain, Reference reference) {
}
