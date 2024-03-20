package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;

import java.util.Set;

public record ReferenceDto(DomainReference reference, Boolean enabled, Set<Domain> alis) {
}
