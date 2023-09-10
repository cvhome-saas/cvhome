package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.FileService;
import com.asrevo.cvhome.certificatemanager.utils.Utils;
import com.asrevo.cvhome.commons.domain.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.*;

@Service
@Lazy
@AllArgsConstructor
@Slf4j
public class AcmCertificateOrderServiceImpl implements AcmCertificateOrderService {

    private final DomainCertificateOrderService domainCertificateOrderService;

    private final AcmeManagerService acmeManagerService;

    private final FileService fileService;

    @Override
    public DomainCertificateOrder initiateOrder(DomainCertificateOrder order) {
        DomainCertificateOrder certificateOrder = DomainCertificateOrder.newOrder();
        certificateOrder.setDomain(order.getDomain());
        certificateOrder.setCertificateOrderStatus(CertificateOrderStatus.INITIATED);
        certificateOrder.setChallengeValidationType(order.getChallengeValidationType());
        certificateOrder.setCreatedDate(Instant.now());
        return domainCertificateOrderService.save(certificateOrder);
    }

    @Override
    public void order(Long orderId) {
        Set<CertificateOrderStatus> preOrderNeededStatus = Set.of(INITIATED);
        domainCertificateOrderService.findOneById(orderId).ifPresent(it -> {
            if (preOrderNeededStatus.contains(it.getCertificateOrderStatus())) {
                try {
                    Order o = acmeManagerService.order(it.getDomain());
                    it.setLocation(o.getLocation().toString());
                    it.setCertificateOrderStatus(CertificateOrderStatus.REQUESTED);
                    it.setRequestedDate(Instant.now());
                    it.setChallenges(new Challenges(AcmeManagerServiceImpl.getChallenges(it.getDomain(), o)));
                    domainCertificateOrderService.save(it);
                } catch (Exception e) {
                    log.error("error when requesting order for order {}", orderId);
                }
            } else {
                log.warn("request to do order certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void doValidation(Long orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(VALIDATION_REQUESTED);
        domainCertificateOrderService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                boolean isValid = Utils.performPreValidation(it.getChallenges().challenges(), it.getChallengeValidationType().getChallenge());
                if (isValid) {
                    CertificateOrderStatus certificateOrderStatus = doAcmValidation(it);
                    log.error("acmValidation validation is {} for domain {}", certificateOrderStatus, orderId);
                    it.setCertificateOrderStatus(certificateOrderStatus);
                } else {
                    log.warn("performPreValidation validation is {} for domain {}", PRE_VALIDATED_INVALID, orderId);
                    it.setCertificateOrderStatus(PRE_VALIDATED_INVALID);
                }
                it.setValidatedDate(Instant.now());
                domainCertificateOrderService.save(it);
            } else {
                log.warn("request to do validation certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void askValidate(DomainCertificateOrder order, String type) {
        if (ChallengeValidationType.valueOf(type).isAutomaticValidation()) {
            throw new UnsupportedOperationException("automatic validation not support request validate operation");
        }
        Set<CertificateOrderStatus> preAskValidateNeededStatus = Set.of(REQUESTED, PRE_VALIDATED_INVALID);
        domainCertificateOrderService.findOneById(order.getId()).map(it -> {
            if (preAskValidateNeededStatus.contains(it.getCertificateOrderStatus())) {
                it.setCertificateOrderStatus(VALIDATION_REQUESTED);
                return domainCertificateOrderService.save(it);
            } else {
                log.error("order {} not requested yet please wait we will notify you once its requested", order.getId());
                throw new RuntimeException("order not requested yet please wait we will notify you once its requested");
            }
        }).orElseThrow(() -> {
            log.error("askValidate for order {} that not exist", order.getId());
            return new RuntimeException("order not found");
        });
    }

    @Override
    public void preValidation(Long orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(REQUESTED);
        domainCertificateOrderService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                if (it.getChallengeValidationType().isAutomaticValidation()) {
                    if (TlsAlpn01Challenge.TYPE.equals(it.getChallengeValidationType().getChallenge())) {
                        try {
                            log.info("will generateTemporalTlsAlpn01Certificate for order {}", orderId);
                            acmeManagerService.generateTemporalTlsAlpn01Certificate(it, fileService::upload);
                            it.setCertificateOrderStatus(VALIDATION_REQUESTED);
                            domainCertificateOrderService.save(it);
                        } catch (IOException e) {
                            log.error("error when generateTemporalTlsAlpn01Certificate for order {}", orderId);
                        }
                    } else if (Http01Challenge.TYPE.equals(it.getChallengeValidationType().getChallenge())) {
                        try {
                            log.info("will generateTemporalHttpValidationFile for order {}", orderId);
                            acmeManagerService.generateTemporalHttpValidationFile(it, fileService::addToken);
                            it.setCertificateOrderStatus(VALIDATION_REQUESTED);
                            domainCertificateOrderService.save(it);
                        } catch (Exception e) {
                            log.error("error when generateTemporalHttpValidationFile for order {}", orderId);
                        }
                    } else if (Dns01Challenge.TYPE.equals(it.getChallengeValidationType().getChallenge())) {
                        // @TODO check if we need to generate something before dns check
                        // send mail to creator to follow steps to prove ownership
                    } else {
                        log.error("unknown {} challenge type for order {}", it.getChallengeValidationType().getChallenge(), it.getId());
                    }
                }
            } else {
                log.warn("request to do generate Validation Challenge certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });

    }

    @Override
    public void doGeneration(Long orderId) {
        Set<CertificateOrderStatus> preGenerationNeededStatus = Set.of(VALIDATED_VALID);
        domainCertificateOrderService.findOneById(orderId).ifPresent(it -> {
            if (preGenerationNeededStatus.contains(it.getCertificateOrderStatus())) {
                log.info("will generate acm certificate for order {}", orderId);
                it.setCertificateOrderStatus(doAcmGeneration(it));
                if (GENERATED.equals(it.getCertificateOrderStatus())) {
                    it.setGeneratedDate(Instant.now());
                } else {
                    log.error("generation failed for order {}", orderId);
                }
                domainCertificateOrderService.save(it);
            } else {
                log.warn("request to do generate certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }


    private CertificateOrderStatus doAcmGeneration(DomainCertificateOrder it) {
        DomainCertificate certificate = null;
        try {
            certificate = acmeManagerService.generate(it, fileService::upload);
        } catch (Exception e) {
            log.error("can not generate certificate for order {} ", it.getId());
        }
        return Optional.ofNullable(certificate)
                .map(d -> CertificateOrderStatus.GENERATED).orElse(CertificateOrderStatus.FAIL_GENERATING);
    }


    private CertificateOrderStatus doAcmValidation(DomainCertificateOrder order) {
        try {
            Status status = acmeManagerService.validate(new URL(order.getLocation()), order.getChallengeValidationType().getChallenge());
            return status == Status.VALID
                    ? CertificateOrderStatus.VALIDATED_VALID
                    : CertificateOrderStatus.VALIDATED_INVALID;
        } catch (Exception e) {
            return CertificateOrderStatus.VALIDATED_INVALID;
        }
    }

}
