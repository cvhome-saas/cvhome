package com.asrevo.cvhome.router.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;

public record AddAlisDto(DomainReference reference, Domain domain) {
}
