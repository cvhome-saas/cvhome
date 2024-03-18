package com.asrevo.cvhome.dcm.commons.dto;


import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.Reference;

public record RegisterDomainRequest(Domain domain, DomainType domainType, Reference reference,
                                    ChallengeValidationType recommendedType, boolean includeSubDomains) {
}
