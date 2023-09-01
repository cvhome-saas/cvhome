package com.asrevo.cvhome.commons.domain;

import com.asrevo.cvhome.commons.domain.DomainStatus;
import com.asrevo.cvhome.commons.domain.DomainType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import java.time.Instant;

public record DomainReference(@Id Long id,
                              String domain,
//                              @CreatedBy
                              String reference,
                              DomainType domainType,
                              DomainStatus domainStatus,
                              @CreatedDate Instant createdDate,
                              Long externalAcmOrderId
) {
}
