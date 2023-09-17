package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge;
import com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge;
import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.OrderDomain;
import org.apache.commons.codec.DecoderException;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface AcmeManagerService {

    Order order(OrderDomain domain) throws AcmeException, IOException;

    Status validate(URL location, String type) throws AcmeException;

    DomainCertificate generate(DomainCertificateOrder certificateOrder)
            throws IOException, AcmeException;

    void generate(OrderDomain domain, TlsAlpn01Challenge challenge)
            throws IOException, DecoderException;

    void generateValidationFile(Http01Challenge challenge) throws IOException;

    InputStream getHttpValidationFile(HttpValidationToken token);

    InputStreamResource getCertificateFile(OrderDomain domain, CertificateFileType fileType);

}
