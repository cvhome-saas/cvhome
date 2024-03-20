package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;
import com.asrevo.cvhome.router.commons.domain.Country;

public record CreateNewReferenceDto(Reference reference, Domain domain, Country country) {
}
