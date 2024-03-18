package com.asrevo.cvhome.dcm.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;

public record DomainChangeReferenceRequest(Domain domain, Reference reference) {
}
