package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.repository.DomainCertificateRepository;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.FileService;
import com.asrevo.cvhome.certificatemanager.service.IDomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.domain.Challenges;
import lombok.AllArgsConstructor;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Lazy
@AllArgsConstructor
public class DomainCertificateOrderServiceImpl implements DomainCertificateOrderService {

    private final String validationTypeFlag = "usedInValidation";

    private final IDomainCertificateOrderService iDomainCertificateOrderService;

    private final DomainCertificateRepository domainCertificateRepository;

    private final AcmeManagerService acmeManagerService;

    private final FileService fileService;


    @Override
    public DomainCertificateOrder order(DomainCertificateOrder order) throws AcmeException, IOException {
        Order o = acmeManagerService.order(order.getDomain());

        DomainCertificateOrder one = iDomainCertificateOrderService.findOneByLocation(o.getLocation().toString());
        if (one != null) {
            return one;
        }
        DomainCertificateOrder certificateOrder = new DomainCertificateOrder();
        certificateOrder.setDomain(order.getDomain());
        certificateOrder.setLocation(o.getLocation().toString());
        certificateOrder.setCertificateOrderStatus(CertificateOrderStatus.REQUESTED);
        certificateOrder.setChallenges(new Challenges(AcmeManagerServiceImpl.getChallenges(order.getDomain(), o)));
        return iDomainCertificateOrderService.save(certificateOrder);
    }

    @Override
    public DomainCertificateOrder validate(DomainCertificateOrder order, String type)
            throws AcmeException, IOException {

        DomainCertificateOrder certificateOrder =
                iDomainCertificateOrderService.findOneByLocation(order.getLocation());

        Map<String, String> challenge = certificateOrder.getChallenges().challenges().get(type);
        if (challenge != null) {
            if (type.equals(TlsAlpn01Challenge.TYPE)) {
                // @TODO should check if dns pointing to my server to generate Temp
                // certification
                acmeManagerService.generateTemporalTlsAlpn01Certificate(certificateOrder, fileService::upload);
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

        return iDomainCertificateOrderService.save(certificateOrder);
    }

    @Override
    public DomainCertificate generate(DomainCertificateOrder certificateOrder) throws AcmeException, IOException {
        DomainCertificateOrder one = iDomainCertificateOrderService.findOneByLocation(certificateOrder.getLocation());
        Status status = Status.INVALID;
        Optional<String> validationType = Optional.empty();

        if (one != null) {
            validationType = one.getChallenges().challenges().entrySet().stream()
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
            DomainCertificate domainCertificate = acmeManagerService.generate(one, fileService::upload);
            one.setCertificateOrderStatus(CertificateOrderStatus.GENERATED);
            iDomainCertificateOrderService.save(one);
            return domainCertificateRepository.put(domainCertificate);
        }
        return DomainCertificate.from(certificateOrder, status.name());
    }
}
