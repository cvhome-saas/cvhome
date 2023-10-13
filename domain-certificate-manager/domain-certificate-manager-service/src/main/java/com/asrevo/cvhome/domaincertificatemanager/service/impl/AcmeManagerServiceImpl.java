package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.domaincertificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmFileService;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmeManagerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.util.function.Supplier;

@Service
@Lazy
@Slf4j
@AllArgsConstructor
public class AcmeManagerServiceImpl implements AcmeManagerService {

    private final Account account;

    private final Login login;

    private final AcmFileService fileService;


    private static void tryUntilTrue(int tries, Supplier<Boolean> tPredicate) {
        int localTries = tries;
        while (localTries > 1 && !tPredicate.get()) {
            localTries--;
        }
    }


    @Override
    public Order order(Domain domain) throws AcmeException, IOException {
        Order order = this.account.newOrder().domains(domain.domain()).create();
        fileService.generateOrGetKeyPair(domain);
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

    private CSRBuilder generateCsr(KeyPair keyPair, Domain domain) throws IOException {
        CSRBuilder csrBuilder = new CSRBuilder();
        csrBuilder.addDomains(domain.domain());
        csrBuilder.sign(keyPair);
        fileService.storeCsr(domain, csrBuilder);
        return csrBuilder;
    }

    @Override
    public DomainCertificate generate(OrderLocation location, Domain domain)
            throws IOException, AcmeException {
        Order order = this.login.bindOrder(location.url());
        KeyPair domainKeyPair = fileService.generateOrGetKeyPair(domain);
        CSRBuilder csrBuilder = generateCsr(domainKeyPair, domain);
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
                fileService.storeCertificate(domain, certificate.getCertificate());
                return new DomainCertificate(certificate);
            }
        }
        return null;
    }

    @Override
    public InputStreamResource getCertificateFile(Domain domain, CertificateFileType fileType) {
        return fileService.getCertificateFile(domain, fileType);
    }

}
