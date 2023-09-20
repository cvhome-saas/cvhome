package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.CertificateId;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.dto.DomainCreateRequestDto;
import com.asrevo.cvhome.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateResponseDto;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

public interface AcmCertificateOrderService {

    DomainCreateResponseDto register(DomainCreateRequestDto createRequest);

    OrdersCreateResponseDto initiateOrder(OrdersCreateRequestDto createRequest);

    OrdersCreateResponseDto initiateOrder(Domain domain);

    void order(OrdersId orderId) throws AcmeException, IOException;

    void doValidation(OrdersId orderId);

    void askValidate(OrdersId orderId, String type)
            throws AcmeException, IOException;

    void preValidation(OrdersId orderId);

    void doGeneration(OrdersId orderId);

    void notifyDomainThatOrderRequested(Domain domain);

    void addNewCertificateToDomain(Domain domain, OrdersId ordersId, CertificateId certificateId);
}
