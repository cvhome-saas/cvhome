package com.asrevo.cvhome.domaincertificatemanager.service.impl;

import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateFileType;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.Domain;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.OrderLocation;
import com.asrevo.cvhome.domaincertificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmFileService;
import com.asrevo.cvhome.domaincertificatemanager.service.AcmeManagerService;
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
import java.util.List;
import java.util.function.Supplier;

@Service
@Lazy
@Slf4j
public class AcmeManagerServiceImpl implements AcmeManagerService {

    private final Account account;

    private final Login login;

    private final AcmFileService fileService;

    public AcmeManagerServiceImpl(Account account, Login login, AcmFileServiceProvider provider) {
        this.account = account;
        this.login = login;
        this.fileService = provider.getInstance();
    }


    private static void tryUntilTrue(int tries, Supplier<Boolean> tPredicate) {
        int localTries = tries;
        while (localTries > 1 && !tPredicate.get()) {
            localTries--;
        }
    }


    @Override
    public Order order(Domain domain, boolean includeSubDomains) throws AcmeException, IOException {
        List<String> domains = includeSubDomains ?
                List.of(domain.domain(), "*." + domain.domain()) : List.of(domain.domain());
        Order order = this.account.newOrder().domains(domains).create();
        fileService.generateOrGetKeyPair(domain);
        return order;
    }

    @Override
    public Status validate(URL location, String type) throws AcmeException {
        Order order = this.login.bindOrder(location);

        boolean allStatusValid = order.getAuthorizations()
                .stream().map(it -> triggerAuthorizationValidation(it, type))
                .allMatch(Status.VALID::equals);

        return allStatusValid ? Status.VALID : Status.INVALID;
    }

    private Status triggerAuthorizationValidation(Authorization authorization, String type) {
        Challenge challenge = authorization.findChallenge(type);
        if (challenge != null) {
            if (challenge.getStatus() == Status.PENDING) {
                try {
                    challenge.trigger();
                } catch (Exception e) {
                    log.error("can't do challenge trigger  " + authorization.getIdentifier().getDomain());
                }
            }
            tryUntilTrue(5, () -> {
                try {
                    if (challenge.getStatus().equals(Status.VALID)) {
                        return true;
                    }
                    Thread.sleep(3000L);
                    challenge.update();
                    if (challenge.getError() != null) {
                        log.error("validation error for " + authorization.getIdentifier().getDomain() + " with error "
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

    private CSRBuilder generateCsr(KeyPair keyPair, Domain domain, boolean includeSubDomains) throws IOException {
        List<String> domains = includeSubDomains ?
                List.of(domain.domain(), "*." + domain.domain()) : List.of(domain.domain());
        CSRBuilder csrBuilder = new CSRBuilder();
        csrBuilder.addDomains(domains);
        csrBuilder.sign(keyPair);
        fileService.storeCsr(domain, csrBuilder);
        return csrBuilder;
    }

    @Override
    public DomainCertificate generate(OrderLocation location, Domain domain, boolean includeSubDomains)
            throws IOException, AcmeException {
        Order order = this.login.bindOrder(location.url());
        KeyPair domainKeyPair = fileService.generateOrGetKeyPair(domain);
        CSRBuilder csrBuilder = generateCsr(domainKeyPair, domain, includeSubDomains);
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
