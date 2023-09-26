package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.dto.DomainCreateRequestDto;
import com.asrevo.cvhome.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateResponseDto;

public interface AcmCertificateOrderService {

    DomainCreateResponseDto register(DomainCreateRequestDto createRequest);

    OrdersCreateResponseDto initiateOrder(OrdersCreateRequestDto createRequest);

    OrdersCreateResponseDto initiateOrder(Domain domain, ChallengeValidationType validationType);

    void order(OrdersId orderId);

    void validate(OrdersId orderId);

    void triggerValidate(OrdersId orderId, String type);

    void prepareOrderValidation(OrdersId orderId);

    void doGeneration(OrdersId orderId);

    void updateDomainStatus(Domain domain);

    void addNewCertificateToDomain(Domain domain, OrdersId ordersId, CertificateId certificateId);
}
