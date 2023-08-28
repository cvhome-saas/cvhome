package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.user.domain.DomainReference;

public record DomainReferenceOrder(DomainReference domainReference, DomainCertificateOrder domainCertificateOrder) {
}
