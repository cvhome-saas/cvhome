package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersCreateRequestDto {
    private Domain domain;
    private ChallengeValidationType challengeValidationType;
}
