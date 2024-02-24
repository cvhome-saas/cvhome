package com.asrevo.cvhome.domaincertificatemanager.service.acm;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.repository.FilesRepository;
import com.asrevo.cvhome.domaincertificatemanager.service.storage.impl.TableFileServiceImpl;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.core.io.InputStreamResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static com.asrevo.cvhome.domaincertificatemanager.entity.FileEntity.resolveFileUri;

public class TableAcmFileServiceImpl implements AcmFileService {
    private final TableFileServiceImpl fileService;
    private final String acmRoot = "acm";

    public TableAcmFileServiceImpl(FilesRepository filesRepository) {
        this.fileService = new TableFileServiceImpl(filesRepository);
    }


    @Override
    public KeyPair generateOrGetKeyPair(Domain domain) throws IOException {
        String fileName = resolveFileUri(this.acmRoot, domain.encoded(), CertificateFileType.KEY.getFile());
        if (fileService.exist(fileName)) {
            InputStream stream = fileService.getFile(fileName);
            return KeyPairUtils.readKeyPair(new InputStreamReader(stream));
        } else {
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);
            Path domainPath = Files.createTempFile("domain", ".key");
            FileWriter fileWriter = new FileWriter(domainPath.toFile());
            KeyPairUtils.writeKeyPair(domainKeyPair, fileWriter);
            fileService.upload(domainPath.toFile(), fileName);
            return domainKeyPair;
        }
    }

    @Override
    public void storeCsr(Domain domain, CSRBuilder csrBuilder) throws IOException {
        File domainCsrTemp = Files.createTempFile("domain", ".csr").toFile();
        FileWriter csrFileWriter = new FileWriter(domainCsrTemp);
        csrBuilder.write(csrFileWriter);
        String fileName = resolveFileUri(this.acmRoot, domain.encoded(), CertificateFileType.CSR.getFile());
        fileService.upload(domainCsrTemp, fileName);
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
        String fileName = resolveFileUri(this.acmRoot, domain.encoded(), CertificateFileType.CRT.getFile());
        fileService.upload(domainCrt, fileName);

    }

    @Override
    public InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType) {
        String fileName = resolveFileUri(this.acmRoot, domain.encoded(), fileType.getFile());
        if (fileService.exist(fileName)) {
            return new InputStreamResource(fileService.getFile(fileName));
        } else {
            return null;
        }
    }
}