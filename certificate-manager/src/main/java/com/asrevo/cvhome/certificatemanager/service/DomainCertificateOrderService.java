package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import org.shredzone.acme4j.exception.AcmeException;

import java.io.IOException;

public interface DomainCertificateOrderService {

    DomainCertificateOrder order(String domain) throws AcmeException, IOException;

    DomainCertificateOrder validate(DomainCertificateOrder certificateOrder, String type)
            throws AcmeException, IOException;

    DomainCertificate generate(DomainCertificateOrder certificateOrder) throws AcmeException, IOException;
}
