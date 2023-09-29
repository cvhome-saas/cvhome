package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrdersId;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersCreateResponseDto;

public interface AcmCertificateOrderService {


    OrdersCreateResponseDto initiateOrder(OrdersCreateRequestDto createRequest);

    OrdersCreateResponseDto initiateOrder(Domain domain, ChallengeValidationType validationType);

    void order(OrdersId orderId);

    void validate(OrdersId orderId);

    void triggerValidate(OrdersId orderId, String type);

    void prepareOrderValidation(OrdersId orderId);

    void doGeneration(OrdersId orderId);
}
