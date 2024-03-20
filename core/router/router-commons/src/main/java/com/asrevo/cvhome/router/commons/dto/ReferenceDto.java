package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Reference;

import java.util.Set;

public record ReferenceDto(Reference reference, Boolean enabled, Set<Domain> alis) {
}
