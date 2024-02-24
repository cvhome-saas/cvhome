package com.asrevo.cvhome.domaincertificatemanager.commons.dto;


import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Reference;

public record RegisterDomainRequest(Domain domain, DomainType domainType, Reference reference,
                                    ChallengeValidationType recommendedType, boolean includeSubDomains) {
}
