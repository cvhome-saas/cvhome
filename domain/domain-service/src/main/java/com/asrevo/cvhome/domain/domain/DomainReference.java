package com.asrevo.cvhome.domain.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.Instant;

public record DomainReference(@Id Long id,
                              String domain,
                              String reference,
                              DomainType domainType,
                              DomainStatus domainStatus,
                              @CreatedDate Instant createdDate,
                              Long externalAcmOrderId) {
}
