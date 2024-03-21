package com.asrevo.cvhome.dcm.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.dcm.commons.domain.DomainCertificateStatus;
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
