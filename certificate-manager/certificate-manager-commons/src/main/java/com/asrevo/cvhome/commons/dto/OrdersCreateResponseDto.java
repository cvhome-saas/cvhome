package com.asrevo.cvhome.commons.dto;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.OrderDomain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class OrdersCreateResponseDto {
    private OrdersId id;
    private OrderDomain domain;
    private ChallengeValidationType challengeValidationType;
    private CertificateOrderStatus certificateOrderStatus;
    private Instant createdDate;
}
