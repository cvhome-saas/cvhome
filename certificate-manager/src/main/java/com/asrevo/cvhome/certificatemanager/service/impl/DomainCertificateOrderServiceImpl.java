package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.repository.DomainCertificateOrderRepository;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.repository.DomainCertificateRepository;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.S3Service;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

@Service
@Lazy
public class DomainCertificateOrderServiceImpl implements DomainCertificateOrderService {

    private final String validationTypeFlag = "usedInValidation";

    private final DomainCertificateOrderRepository domainCertificateOrderRepository;

    private final DomainCertificateRepository domainCertificateRepository;

    private final AcmeManagerService acmeManagerService;

    private final S3Service s3Service;

    public DomainCertificateOrderServiceImpl(
            DomainCertificateOrderRepository domainCertificateOrderRepository,
            DomainCertificateRepository domainCertificateRepository,
            AcmeManagerService acmeManagerService,
            S3Service s3Service) {
        this.domainCertificateOrderRepository = domainCertificateOrderRepository;
        this.domainCertificateRepository = domainCertificateRepository;
        this.acmeManagerService = acmeManagerService;
        this.s3Service = s3Service;
    }

    @Override
    public DomainCertificateOrder order(String domain) throws AcmeException, IOException {
        Order order = acmeManagerService.order(domain);

        DomainCertificateOrder certificateOrder = DomainCertificateOrder.builder()
                .domain(domain)
                .location(order.getLocation().toString())
                .certificateOrderStatus(CertificateOrderStatus.REQUESTED)
                .challenges(AcmeManagerServiceImpl.getChallenges(domain, order))
                .build();
        return domainCertificateOrderRepository.put(certificateOrder);
    }

    @Override
    public DomainCertificateOrder validate(DomainCertificateOrder order, String type)
            throws AcmeException, IOException {

        DomainCertificateOrder certificateOrder =
                domainCertificateOrderRepository.findOneByLocation(order.getLocation());

        Map<String, String> challenge = certificateOrder.getChallenges().get(type);
        if (challenge != null) {
            if (type.equals(TlsAlpn01Challenge.TYPE)) {
                // @TODO should check if dns pointing to my server to generate Temp
                // certification
                acmeManagerService.generateTemporalTlsAlpn01Certificate(certificateOrder, s3Service::uploadFile);
            }
            Status status = acmeManagerService.validate(new URL(certificateOrder.getLocation()), type);

            CertificateOrderStatus orderStatus = status == Status.VALID
                    ? CertificateOrderStatus.VALIDATED_VALID
                    : CertificateOrderStatus.VALIDATED_INVALID;
            challenge.put(validationTypeFlag, "true");
            certificateOrder.setCertificateOrderStatus(orderStatus);
        } else {
            certificateOrder.setCertificateOrderStatus(CertificateOrderStatus.VALIDATED_INVALID);
        }

        return domainCertificateOrderRepository.put(certificateOrder);
    }

    @Override
    public DomainCertificate generate(DomainCertificateOrder certificateOrder) throws AcmeException, IOException {
        DomainCertificateOrder one = domainCertificateOrderRepository.findOneByLocation(certificateOrder.getLocation());
        Status status = Status.INVALID;
        Optional<String> validationType = Optional.empty();

        if (one != null) {
            validationType = one.getChallenges().entrySet().stream()
                    .filter(challenges -> {
                        // check if this challenge used in the validation process
                        return challenges.getValue().entrySet().stream().anyMatch(challenge -> {
                            return challenge.getKey().equals(validationTypeFlag)
                                    && challenge.getValue().equals("true");
                        });
                    })
                    .findFirst()
                    .map(Map.Entry::getKey);
        }

        if (one != null && validationType.isPresent()) {
            status = acmeManagerService.validate(new URL(one.getLocation()), validationType.get());
        }
        if (one != null && status == Status.VALID) {
            DomainCertificate domainCertificate = acmeManagerService.generate(one, s3Service::uploadFile);
            one.setCertificateOrderStatus(CertificateOrderStatus.GENERATED);
            domainCertificateOrderRepository.put(one);
            return domainCertificateRepository.put(domainCertificate);
        }
        return DomainCertificate.from(certificateOrder, status.name());
    }
}
