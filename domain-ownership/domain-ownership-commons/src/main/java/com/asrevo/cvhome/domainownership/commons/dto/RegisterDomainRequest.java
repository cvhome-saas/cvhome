package com.asrevo.cvhome.domainownership.commons.dto;


import com.asrevo.cvhome.domainownership.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.domain.Reference;

public record RegisterDomainRequest(Domain domain, Reference reference) {
}
