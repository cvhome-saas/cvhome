package com.asrevo.cvhome.dcm.commons.dto;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersCreateRequestDto {
    private Domain domain;
    private ChallengeValidationType challengeValidationType;
}
