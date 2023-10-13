package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersCreateRequestDto {
    private Domain domain;
    private ChallengeValidationType challengeValidationType;
}
