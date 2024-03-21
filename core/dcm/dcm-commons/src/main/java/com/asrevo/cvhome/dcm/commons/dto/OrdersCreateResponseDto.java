package com.asrevo.cvhome.dcm.commons.dto;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
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
