package com.asrevo.cvhome.domainownership.commons.domain;

import org.springframework.data.relational.core.mapping.Column;

public record Reference(@Column("reference") String reference, @Column("reference_type") ReferenceType referenceType) {
}
