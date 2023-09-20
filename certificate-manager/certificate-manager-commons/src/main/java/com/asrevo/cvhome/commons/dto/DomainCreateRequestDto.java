package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainCreateRequestDto {
    private Domain domain;
    private boolean autoRenew;
}
