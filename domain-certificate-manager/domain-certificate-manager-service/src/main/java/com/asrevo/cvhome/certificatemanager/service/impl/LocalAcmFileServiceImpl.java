package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.certificatemanager.service.AcmFileService;
import lombok.SneakyThrows;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

@Service
public class LocalAcmFileServiceImpl implements AcmFileService {
    private final LocalFileServiceImpl fileService;
    private Path acmRoot;

    public LocalAcmFileServiceImpl(LocalFileServiceImpl fileService) {
        this.fileService = fileService;
        acmRoot = createAcmBaseDirectory();
    }

    @SneakyThrows
    private static void createParentDirectory(Path destination) {
        if (!destination.getParent().toFile().exists()) {
            Files.createDirectories(destination.getParent());
        }
    }

    private Path createAcmBaseDirectory() {
        this.acmRoot = Paths.get(System.getProperty("user.home"), "cvhome/certificate-manager/acm");
        createParentDirectory(this.acmRoot);
        return this.acmRoot;
    }


    @Override
    public KeyPair generateOrGetKeyPair(Domain domain) throws IOException {
        Path domainKeyPath = acmRoot.resolve(Paths.get(domain.encoded(), CertificateFileType.KEY.getFile()));
        if (fileService.exist(domainKeyPath.toString())) {
            InputStream stream = fileService.getFile(domainKeyPath.toString());
            return KeyPairUtils.readKeyPair(new InputStreamReader(stream));
        } else {
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);
            Path domainPath = Files.createTempFile("domain", ".key");
            FileWriter fileWriter = new FileWriter(domainPath.toFile());
            KeyPairUtils.writeKeyPair(domainKeyPair, fileWriter);
            fileService.upload(domainPath.toFile(), domainKeyPath.toString());
            return domainKeyPair;
        }
    }

    @Override
    public void storeCsr(Domain domain, CSRBuilder csrBuilder) throws IOException {
        File domainCsrTemp = Files.createTempFile("domain", ".csr").toFile();
        FileWriter csrFileWriter = new FileWriter(domainCsrTemp);
        csrBuilder.write(csrFileWriter);
        Path csrPath = acmRoot.resolve(Paths.get(domain.encoded(), CertificateFileType.CSR.getFile()));
        fileService.upload(domainCsrTemp, csrPath.toString());

    }

    @Override
    public void storeCertificate(Domain domain, X509Certificate... certificates) throws IOException {
        File domainCrt = Files.createTempFile("domain", ".crt").toFile();
        try (FileWriter crtFileWriter = new FileWriter(domainCrt)) {
            try {
                for (X509Certificate cert : certificates) {
                    AcmeUtils.writeToPem(cert.getEncoded(), AcmeUtils.PemLabel.CERTIFICATE, crtFileWriter);
                }
            } catch (CertificateEncodingException ex) {
                throw new IOException("Encoding error", ex);
            }
        }
        Path certificatePath = acmRoot.resolve(Paths.get(domain.encoded(), CertificateFileType.CRT.getFile()));
        fileService.upload(domainCrt, certificatePath.toString());
    }

    @Override
    public InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType) {
        String fileName = acmRoot.resolve(Paths.get(domain.encoded(), fileType.getFile())).toString();
        if (fileService.exist(fileName)) {
            return new InputStreamResource(fileService.getFile(fileName));
        } else {
            return null;
        }
    }
}
