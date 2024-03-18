package com.asrevo.cvhome.domaincertificatemanager.commons.dto;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersCreateResponseDto {
    private OrdersId id;
    private Domain domain;
    private ChallengeValidationType challengeValidationType;
    private CertificateOrderStatus certificateOrderStatus;
    private Instant createdDate;
}
