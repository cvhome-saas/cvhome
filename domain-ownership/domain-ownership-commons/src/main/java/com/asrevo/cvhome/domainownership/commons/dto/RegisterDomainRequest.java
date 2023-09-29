package com.asrevo.cvhome.domainownership.commons.dto;


import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domainownership.commons.domain.DomainType;
import com.asrevo.cvhome.domainownership.commons.domain.Reference;

public record RegisterDomainRequest(Domain domain, DomainType domainType, Reference reference) {
}
