package com.asrevo.cvhome.certificatemanager.commons.dto;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainCertificateStatus;
import com.asrevo.cvhome.certificatemanager.commons.domain.DomainId;
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
