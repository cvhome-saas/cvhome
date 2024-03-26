package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.Country;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;

public record CreateNewReferenceDto(DomainReference reference, Domain domain, Country country) {
}
