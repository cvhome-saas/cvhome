package com.asrevo.cvhome.certificatemanager.commons.dto;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersCreateRequestDto {
    private Domain domain;
    private ChallengeValidationType challengeValidationType;
}
