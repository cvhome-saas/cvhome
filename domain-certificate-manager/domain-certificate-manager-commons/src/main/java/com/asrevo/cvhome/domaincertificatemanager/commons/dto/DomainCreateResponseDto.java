package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.DomainId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainCreateResponseDto {
    private DomainId id;
    private Domain domain;
    private DomainCertificateStatus status;
    private boolean autoRenew;
    private boolean autoOrder;
}
