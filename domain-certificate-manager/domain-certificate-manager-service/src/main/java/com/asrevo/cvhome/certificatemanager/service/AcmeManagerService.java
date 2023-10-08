package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface AcmeManagerService {

    Order order(Domain domain) throws AcmeException, IOException;

    Status validate(URL location, String type) throws AcmeException;

    DomainCertificate generate(OrderLocation location, Domain domain)
            throws IOException, AcmeException;

    InputStream getHttpValidationFile(HttpValidationToken token);

    InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType);

}
