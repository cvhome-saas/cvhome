package com.asrevo.cvhome.certificatemanager.commons.dto;

import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainCreateRequestDto {
    private Domain domain;
    private boolean autoRenew;
    private boolean autoOrder;
}
