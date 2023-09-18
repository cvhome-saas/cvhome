package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateResponseDto;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

public interface AcmCertificateOrderService {

    OrdersCreateResponseDto initiateOrder(OrdersCreateRequestDto createRequest);

    void order(OrdersId orderId) throws AcmeException, IOException;

    void doValidation(OrdersId orderId);

    void askValidate(OrdersId orderId, String type)
            throws AcmeException, IOException;

    void preValidation(OrdersId orderId);

    void doGeneration(OrdersId orderId);
}
