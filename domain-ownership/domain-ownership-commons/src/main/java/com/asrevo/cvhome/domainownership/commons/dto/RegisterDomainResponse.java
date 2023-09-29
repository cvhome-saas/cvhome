package com.asrevo.cvhome.domainownership.commons.dto;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.domain.Reference;

public record RegisterDomainResponse(Domain domain, Reference reference) {
}
