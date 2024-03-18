package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;

public record RegisterDomainResponse(Domain domain, Reference reference) {
}
