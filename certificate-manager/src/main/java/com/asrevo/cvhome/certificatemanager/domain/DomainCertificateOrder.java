package com.asrevo.cvhome.certificatemanager.domain;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("domain_certificate_order")
@Builder
public class DomainCertificateOrder {
    private @Id Long id;
    private String location;
    @NotNull
    private String domain;
    @NotNull
    private String reference;

    private CertificateOrderStatus certificateOrderStatus;
    private Challenges challenges;
}
