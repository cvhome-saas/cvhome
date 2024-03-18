package com.asrevo.cvhome.dcm.service;

import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import com.asrevo.cvhome.dcm.commons.dto.OrdersCreateResponseDto;
import com.asrevo.cvhome.dcm.domain.HttpValidationToken;

import java.io.InputStream;

public interface AcmCertificateOrderService {

    OrdersCreateResponseDto initiate(Domain domain, ChallengeValidationType validationType, boolean includeSubDomains);

    void order(OrdersId orderId);

    void validate(OrdersId orderId);

    void triggerValidate(OrdersId orderId, String type);

    void doGeneration(OrdersId orderId);

    InputStream getHttpValidationToken(HttpValidationToken validationToken);
}
