package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public interface AcmFileService {
    KeyPair generateOrGetKeyPair(Domain domain) throws IOException;

    void storeCsr(Domain domain, CSRBuilder csrBuilder) throws IOException;

    void storeCertificate(Domain domain, X509Certificate... certificates) throws IOException;

    InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType);
}
