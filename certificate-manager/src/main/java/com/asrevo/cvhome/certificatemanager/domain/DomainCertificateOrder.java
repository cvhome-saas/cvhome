package com.asrevo.cvhome.certificatemanager.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class DomainCertificateOrder {

    private String domain;

    private String location;

    private CertificateOrderStatus certificateOrderStatus;

    private Map<String, Map<String, String>> challenges;
}
