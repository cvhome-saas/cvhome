package com.asrevo.cvhome.dcm.service.acm;

import com.asrevo.cvhome.dcm.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.dcm.service.storage.impl.S3FileServiceImpl;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.core.io.InputStreamResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static com.asrevo.cvhome.dcm.domain.S3KeyPath.resolveS3Uri;

public class S3AcmFileServiceImpl implements AcmFileService {
    private final S3FileServiceImpl fileService;
    private final String certStorageBucket;
    private Path acmRoot;

    public S3AcmFileServiceImpl(String certStorageBucket) {
        this.certStorageBucket = certStorageBucket;
        fileService = new S3FileServiceImpl();
        this.init();
    }

    private void init() {
        acmRoot = Paths.get(certStorageBucket, "acm");
    }

    @Override
    public KeyPair generateOrGetKeyPair(Domain domain) throws IOException {
        String fileName = resolveS3Uri(Paths.get(domain.encoded(), CertificateFileType.KEY.getFile()).toString(), this.acmRoot);
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
        String fileName = resolveS3Uri(Paths.get(domain.encoded(), CertificateFileType.CSR.getFile()).toString(), this.acmRoot);
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
        String fileName = resolveS3Uri(Paths.get(domain.encoded(), CertificateFileType.CRT.getFile()).toString(), this.acmRoot);
        fileService.upload(domainCrt, fileName);

    }

    @Override
    public InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType) {
        String fileName = resolveS3Uri(Paths.get(domain.encoded(), fileType.getFile()).toString(), this.acmRoot);
        if (fileService.exist(fileName)) {
            return new InputStreamResource(fileService.getFile(fileName));
        } else {
            return null;
        }
    }

}