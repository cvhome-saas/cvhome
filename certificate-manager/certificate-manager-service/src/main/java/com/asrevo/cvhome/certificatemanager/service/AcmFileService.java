package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge;
import com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge;
import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.OrderDomain;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public interface AcmFileService {
    KeyPair generateOrGetKeyPair(OrderDomain domain) throws IOException;

    void storeCsr(OrderDomain domain, CSRBuilder csrBuilder) throws IOException;

    void storeCertificate(OrderDomain domain, X509Certificate... certificates) throws IOException;

    void generateCertificate(OrderDomain domain, TlsAlpn01Challenge tlsAlpn01Challenge) throws IOException;

    void generateValidationFile(Http01Challenge http01Challenge) throws IOException;

    InputStream getHttpValidationFile(HttpValidationToken token);

    InputStreamResource getCertificateFile(OrderDomain domain, CertificateFileType fileType);
}
