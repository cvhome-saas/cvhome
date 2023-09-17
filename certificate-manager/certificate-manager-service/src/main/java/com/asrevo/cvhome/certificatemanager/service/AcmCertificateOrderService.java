package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

public interface AcmCertificateOrderService {

    DomainCertificateOrder initiateOrder(DomainCertificateOrder order);

    void order(Long orderId) throws AcmeException, IOException;

    void doValidation(Long id);

    void askValidate(DomainCertificateOrder certificateOrder, String type)
            throws AcmeException, IOException;

    void preValidation(Long orderId);

    void doGeneration(Long orderId);
}
