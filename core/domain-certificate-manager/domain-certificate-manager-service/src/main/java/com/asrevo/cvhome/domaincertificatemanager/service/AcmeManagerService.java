package com.asrevo.cvhome.domaincertificatemanager.service;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.domaincertificatemanager.domain.DomainCertificate;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.net.URL;

public interface AcmeManagerService {

    Order order(Domain domain, boolean includeSubDomains) throws AcmeException, IOException;

    Status validate(URL location, String type) throws AcmeException;

    DomainCertificate generate(OrderLocation location, Domain domain, boolean includeSubDomains)
            throws IOException, AcmeException;

    InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType);

}
