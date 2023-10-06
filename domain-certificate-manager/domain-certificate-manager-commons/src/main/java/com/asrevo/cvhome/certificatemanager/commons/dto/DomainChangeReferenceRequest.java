package com.asrevo.cvhome.certificatemanager.commons.dto;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.Reference;

public record DomainChangeReferenceRequest(Domain domain, Reference reference) {
}
