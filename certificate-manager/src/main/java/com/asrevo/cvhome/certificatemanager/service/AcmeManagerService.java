package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.util.function.BiConsumer;

public interface AcmeManagerService {

    Order order(String domain) throws AcmeException, IOException;

    Status validate(URL location, String type) throws AcmeException;

    DomainCertificate generate(DomainCertificateOrder certificateOrder, BiConsumer<File, String> writer)
            throws IOException, AcmeException;

    KeyPair generateOrGetDomainKeyPair(String domain) throws IOException;

    DomainCertificate info(DomainCertificateOrder certificateOrder) throws MalformedURLException;

    void generateTemporalTlsAlpn01Certificate(DomainCertificateOrder certificateOrder, BiConsumer<File, String> writer)
            throws IOException;

    InputStreamResource getDomainCertificateFile(String domain, CertificateFileType fileType);

}
