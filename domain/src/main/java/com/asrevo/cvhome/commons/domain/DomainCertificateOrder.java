package com.asrevo.cvhome.commons.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("domain_certificate_order")
@NoArgsConstructor
@AllArgsConstructor
public class DomainCertificateOrder {
    private @Id Long id;
    private String location;
    @NotNull
    private String domain;

    private CertificateOrderStatus certificateOrderStatus;
    private Challenges challenges;
}
