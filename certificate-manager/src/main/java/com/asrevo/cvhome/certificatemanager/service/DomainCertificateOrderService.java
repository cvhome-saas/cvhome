package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.DomainCertificate;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

public interface DomainCertificateOrderService {

    DomainCertificateOrder order(DomainCertificateOrder order) throws AcmeException, IOException;

    DomainCertificateOrder validate(DomainCertificateOrder certificateOrder, String type)
            throws AcmeException, IOException;

    DomainCertificate generate(DomainCertificateOrder certificateOrder) throws AcmeException, IOException;
}
