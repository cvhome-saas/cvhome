package com.asrevo.cvhome.domaincertificatemanager.commons.domain;

import org.springframework.data.relational.core.mapping.Column;

public record Reference(@Column("reference") String reference, @Column("reference_type") ReferenceType referenceType) {
}
