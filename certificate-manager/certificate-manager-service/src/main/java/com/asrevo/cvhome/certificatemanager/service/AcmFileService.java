package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Http01Challenge;
import com.asrevo.cvhome.certificatemanager.domain.challenges.TlsAlpn01Challenge;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public interface AcmFileService {
    KeyPair generateOrGetKeyPair(Domain domain) throws IOException;

    void storeCsr(Domain domain, CSRBuilder csrBuilder) throws IOException;

    void storeCertificate(Domain domain, X509Certificate... certificates) throws IOException;

    void generateCertificate(Domain domain, TlsAlpn01Challenge tlsAlpn01Challenge) throws IOException;

    void generateValidationFile(Http01Challenge http01Challenge) throws IOException;

    InputStream getHttpValidationFile(HttpValidationToken token);

    InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType);
}
