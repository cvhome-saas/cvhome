package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.common.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.common.domain.DomainRequest;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.toolbox.AcmeUtils;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.CertificateUtils;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.asrevo.cvhome.certificatemanager.utils.Utils.getDomainCode;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Service
@Lazy
@Slf4j
public class AcmeManagerServiceImpl implements AcmeManagerService {

    private final Account account;

    private final Login login;

    private final FileService fileService;

    public AcmeManagerServiceImpl(Account account, Login login, FileService fileService) {
        this.account = account;
        this.login = login;
        this.fileService = fileService;
    }

    private static void storeCertificate(
            BiConsumer<File, String> writer, DomainRequest domainRequest, X509Certificate... certificates)
            throws IOException {
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
        writer.accept(
                domainCrt,
                Paths.get(getDomainCode(domainRequest.getDomain()), "domain.crt")
                        .toString());
    }

    private static <T> void tryUntilTrue(int tries, Supplier<Boolean> tPredicate) {
        int localTries = tries;
        while (localTries > 1 && !tPredicate.get()) {
            localTries--;
        }
    }

    public static Map<String, Map<String, String>> getChallenges(String domain, Order order) {
        Authorization authorization =
                order.getAuthorizations().stream().findFirst().orElseThrow();
        Map<String, Map<String, String>> challenges = new HashMap<>();
        Dns01Challenge dns01Challenge = authorization.findChallenge(Dns01Challenge.TYPE);
        if (dns01Challenge != null) {
            challenges.put(
                    Dns01Challenge.TYPE,
                    Map.of(
                            "key",
                            String.format(
                                    "_acme-challenge.%s",
                                    authorization.getIdentifier().getDomain()),
                            "value",
                            dns01Challenge.getDigest()));
        }

        Http01Challenge http01Challenge = authorization.findChallenge(Http01Challenge.TYPE);
        if (http01Challenge != null) {
            String key = String.format(
                    "http://%s/.well-known/acme-challenge/%s",
                    authorization.getIdentifier().getDomain(), http01Challenge.getToken());
            challenges.put(Http01Challenge.TYPE, Map.of("key", key, "value", http01Challenge.getAuthorization()));
        }

        TlsAlpn01Challenge tlsAlpn01Challenge = authorization.findChallenge(TlsAlpn01Challenge.TYPE);
        if (tlsAlpn01Challenge != null) {
            challenges.put(
                    TlsAlpn01Challenge.TYPE,
                    Map.of("key", domain, "value", encodeHexString(tlsAlpn01Challenge.getAcmeValidation())));
        }
        return challenges;
    }

    @Override
    public Order order(String domain) throws AcmeException, IOException {
        Order order = this.account.newOrder().domains(domain).create();
        generateOrGetDomainKeyPair(domain);
        return order;
    }

    @Override
    public Status validate(URL location, String type) throws AcmeException {
        Order order = this.login.bindOrder(location);
        Authorization authorization =
                order.getAuthorizations().stream().findFirst().orElseThrow();
        Challenge challenge = authorization.findChallenge(type);
        String prefix = (authorization.isWildcard() ? "*." : "")
                + authorization.getIdentifier().getDomain();
        if (challenge != null) {
            if (challenge.getStatus() == Status.PENDING) {
                challenge.trigger();
            }
            tryUntilTrue(5, () -> {
                try {
                    if (challenge.getStatus().equals(Status.VALID)) {
                        return true;
                    }
                    Thread.sleep(3000L);
                    challenge.update();
                    if (challenge.getError() != null) {
                        log.error("validation error for " + prefix + " with error "
                                + challenge.getError().asJSON().toString());
                    }
                    return challenge.getStatus().equals(Status.VALID);
                } catch (Exception e) {
                    return false;
                }
            });
            return challenge.getStatus();
        }
        return Status.INVALID;
    }

    private CSRBuilder generateCsr(KeyPair keyPair, DomainRequest domainRequest) throws IOException {
        CSRBuilder csrBuilder = new CSRBuilder();
        csrBuilder.addDomains(domainRequest.getDomain());
        csrBuilder.sign(keyPair);
        return csrBuilder;
    }

    @Override
    public DomainCertificate generate(DomainCertificateOrder certificateOrder, BiConsumer<File, String> writer)
            throws IOException, AcmeException {
        DomainRequest domainRequest = new DomainRequest();
        domainRequest.setDomain(certificateOrder.getDomain());
        domainRequest.setLocation(new URL(certificateOrder.getLocation()));
        Order order = this.login.bindOrder(domainRequest.getLocation());
        KeyPair domainKeyPair = generateOrGetDomainKeyPair(certificateOrder.getDomain());
        CSRBuilder csrBuilder = generateCsr(domainKeyPair, domainRequest);
        File domainCsr = Files.createTempFile("domain", ".csr").toFile();
        FileWriter csrFileWriter = new FileWriter(domainCsr);
        csrBuilder.write(csrFileWriter);
        writer.accept(
                domainCsr,
                Paths.get(getDomainCode(domainRequest.getDomain()), "domain.csr")
                        .toString());
        order.execute(csrBuilder.getEncoded());
        tryUntilTrue(10, () -> {
            try {
                order.update();
                if (order.getStatus().equals(Status.VALID)) {
                    return true;
                }
                Thread.sleep(1000);
                return order.getStatus().equals(Status.VALID);
            } catch (Exception e) {
                return false;
            }
        });

        if (order.getStatus() == Status.VALID) {
            Certificate certificate = order.getCertificate();
            if (certificate != null) {
                storeCertificate(writer, domainRequest, certificate.getCertificate());
                return new DomainCertificate(
                        certificateOrder, certificate, order.getStatus().name());
            }
        }
        return DomainCertificate.from(certificateOrder, order.getStatus().name());
    }

    @Override
    public KeyPair generateOrGetDomainKeyPair(String domain) throws IOException {
        Path domainKey = Paths.get(getDomainCode(domain), "domain.key");
        if (fileService.exist(domainKey.toString())) {
            InputStream stream = fileService.getFile(domainKey.toString());
            return KeyPairUtils.readKeyPair(new InputStreamReader(stream));
        } else {
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);
            Path domainPath = Files.createTempFile("domain", ".key");
            FileWriter fileWriter = new FileWriter(domainPath.toFile());
            KeyPairUtils.writeKeyPair(domainKeyPair, fileWriter);
            fileService.upload(domainPath.toFile(), domainKey.toString());
            return domainKeyPair;
        }
    }

    @Override
    public DomainCertificate info(DomainCertificateOrder certificateOrder) throws MalformedURLException {
        Order order = this.login.bindOrder(new URL(certificateOrder.getLocation()));
        return new DomainCertificate(
                certificateOrder, order.getCertificate(), order.getStatus().name());
    }

    @Override
    public void generateTemporalTlsAlpn01Certificate(
            DomainCertificateOrder certificateOrder, BiConsumer<File, String> writer) throws IOException {
        URL location = new URL(certificateOrder.getLocation());
        Order order = this.login.bindOrder(location);
        Authorization authorization =
                order.getAuthorizations().stream().findFirst().orElseThrow();
        TlsAlpn01Challenge challenge = authorization.findChallenge(TlsAlpn01Challenge.class);
        if (challenge != null) {
            KeyPair keyPair = generateOrGetDomainKeyPair(certificateOrder.getDomain());
            X509Certificate cert = CertificateUtils.createTlsAlpn01Certificate(
                    keyPair, authorization.getIdentifier(), challenge.getAcmeValidation());
            DomainRequest domainRequest = new DomainRequest();
            domainRequest.setLocation(location);
            domainRequest.setDomain(certificateOrder.getDomain());
            storeCertificate(writer, domainRequest, cert);
        }
    }


    @Override
    public InputStreamResource getDomainCertificateFile(String domain, CertificateFileType fileType) {
        String fileName = Paths.get(getDomainCode(domain), fileType.getFile()).toString();
        return new InputStreamResource(fileService.getFile(fileName));
    }

}
