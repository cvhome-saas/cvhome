package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenges;
import com.asrevo.cvhome.certificatemanager.entity.DomainEntity;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.certificatemanager.mappers.DomainMappers;
import com.asrevo.cvhome.certificatemanager.mappers.OrdersMappers;
import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.certificatemanager.service.OrdersService;
import com.asrevo.cvhome.commons.command.order.AddCertificateToDomainCommand;
import com.asrevo.cvhome.commons.command.order.CreateOrderCommand;
import com.asrevo.cvhome.commons.command.order.ValidateOrderCommand;
import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.dto.DomainCreateRequestDto;
import com.asrevo.cvhome.commons.dto.DomainCreateResponseDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.commons.dto.OrdersCreateResponseDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.challenge.TlsAlpn01Challenge;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.*;

@Service
@Lazy
@AllArgsConstructor
@Slf4j
public class AcmCertificateOrderServiceImpl implements AcmCertificateOrderService {

    private final OrdersService ordersService;

    private final AcmeManagerService acmeManagerService;

    private final StreamBridge streamBridge;

    private final OrdersMappers ordersEntityMappers;

    private final DomainMappers domainMappers;

    private final DomainService domainService;


    @Override
    public DomainCreateResponseDto register(DomainCreateRequestDto createRequest) {
        DomainEntity domain = DomainEntity.createDomain(createRequest.getDomain(), createRequest.isAutoRenew(), createRequest.isAutoOrder());
        DomainEntity savedDomain = domainService.save(domain);
        if (domain.isAutoOrder()) {
            CreateOrderCommand createOrderCommand = new CreateOrderCommand();
            createOrderCommand.setDomain(createOrderCommand.getDomain());
            streamBridge.send("logOrderCommands-out-0", createOrderCommand);
        }
        return domainMappers.toDomainCreateResponse(savedDomain);
    }

    @Override
    public OrdersCreateResponseDto initiateOrder(OrdersCreateRequestDto createRequest) {
        return this.initiateOrder(createRequest.getDomain());
    }

    @Override
    public OrdersCreateResponseDto initiateOrder(Domain domain) {
        DomainEntity domainEntity = domainService.findOneByDomain(domain);
        if (domainEntity == null) {
            throw new RuntimeException("this domain not existed in domain-certificate-manager system");
        }
        OrdersEntity certificateOrder = OrdersEntity.createOrder(domainEntity.getDomain(), domainEntity.getDomain().getRecommendedChallengeValidationType());
        OrdersEntity savedOrder = ordersService.save(certificateOrder);
        return ordersEntityMappers.toOrdersCreateResponse(savedOrder);
    }

    @Override
    public void order(OrdersId orderId) {
        Set<CertificateOrderStatus> preOrderNeededStatus = Set.of(INITIATED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preOrderNeededStatus.contains(it.getCertificateOrderStatus())) {
                try {
                    Order o = acmeManagerService.order(it.getDomain());
                    it.requestOrder(new OrderLocation(o.getLocation()), new Challenges(o));
                    ordersService.save(it);
                } catch (Exception e) {
                    log.error("error when requesting order for order {}", orderId);
                }
            } else {
                log.warn("request to do order certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void doValidation(OrdersId orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(VALIDATION_REQUESTED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                boolean isValid = it.getChallenges().validate(it.getChallengeValidationType());
                if (isValid) {
                    CertificateOrderStatus certificateOrderStatus = doAcmValidation(it);
                    log.info("acmValidation validation is {} for domain {}", certificateOrderStatus, orderId);
                    it.validated(certificateOrderStatus);
                } else {
                    log.warn("performPreValidation validation is {} for domain {}", PRE_VALIDATED_INVALID, orderId);
                    it.validated(PRE_VALIDATED_INVALID);
                }
                ordersService.save(it);
            } else {
                log.warn("request to do validation certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void askValidate(OrdersId orderId, String type) {
        if (ChallengeValidationType.valueOf(type).isAutomaticValidation()) {
            throw new UnsupportedOperationException("automatic validation not support request validate operation");
        }
        Set<CertificateOrderStatus> preAskValidateNeededStatus = Set.of(REQUESTED, PRE_VALIDATED_INVALID);
        ordersService.findOneById(orderId).map(it -> {
            if (preAskValidateNeededStatus.contains(it.getCertificateOrderStatus())) {
                it.requestValidate();
                ValidateOrderCommand command = new ValidateOrderCommand();
                command.setId(it.getId());
                OrdersEntity saved = ordersService.save(it);
                streamBridge.send("logOrderCommands-out-0", command);
                return saved;
            } else {
                log.error("order {} not requested yet please wait we will notify you once its requested", orderId);
                throw new RuntimeException("order not requested yet please wait we will notify you once its requested");
            }
        }).orElseThrow(() -> {
            log.error("askValidate for order {} that not exist", orderId);
            return new RuntimeException("order not found");
        });
    }

    @Override
    public void preValidation(OrdersId orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(REQUESTED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                if (it.getChallengeValidationType().isAutomaticValidation()) {
                    if (TlsAlpn01Challenge.TYPE.equals(it.getChallengeValidationType().getChallenge())) {
                        try {
                            log.info("will generateTemporalTlsAlpn01Certificate for order {}", orderId);
                            acmeManagerService.generate(it.getDomain(), it.getChallenges().getTlsAlpn01Challenge());
                            it.requestValidate();
                            ValidateOrderCommand command = new ValidateOrderCommand();
                            command.setId(it.getId());
                            ordersService.save(it);
                            streamBridge.send("logOrderCommands-out-0", command);
                        } catch (IOException | DecoderException e) {
                            log.error("error when generateTemporalTlsAlpn01Certificate for order {}", orderId);
                        }
                    } else if (Http01Challenge.TYPE.equals(it.getChallengeValidationType().getChallenge())) {
                        try {
                            log.info("will generateTemporalHttpValidationFile for order {}", orderId);
                            acmeManagerService.generateValidationFile(it.getChallenges().getHttp01Challenge());
                            it.requestValidate();
                            ValidateOrderCommand command = new ValidateOrderCommand();
                            command.setId(it.getId());
                            ordersService.save(it);
                            streamBridge.send("logOrderCommands-out-0", command);
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
    public void doGeneration(OrdersId orderId) {
        Set<CertificateOrderStatus> preGenerationNeededStatus = Set.of(VALIDATED_VALID);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preGenerationNeededStatus.contains(it.getCertificateOrderStatus())) {
                log.info("will generate acm certificate for order {}", orderId);
                it.generateOrderCertificate(doAcmGeneration(it));
                ordersService.save(it);
                if (GENERATED.equals(it.getCertificateOrderStatus())) {
                    AddCertificateToDomainCommand command = new AddCertificateToDomainCommand();
                    command.setDomain(it.getDomain());
                    command.setCertificateId(it.getCertificate().getId());
                    command.setOrdersId(orderId);
                    streamBridge.send("logOrderCommands-out-0", command);
                } else {
                    log.error("generation failed for order {}", orderId);
                }

            } else {
                log.warn("request to do generate certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void notifyDomainThatOrderRequested(Domain domain) {
        DomainEntity domainEntity = domainService.findOneByDomain(domain);
        if (domainEntity != null) {
            DomainCertificateStatus newCertificateStatus = switch (domainEntity.getStatus()) {
                case RENEWING_ORDER_PROCESS, EXPIRED_CERTIFICATE -> DomainCertificateStatus.RENEWING_ORDER_PROCESS;
                default -> DomainCertificateStatus.FIRST_ORDERING;
            };
            domainEntity.setStatus(newCertificateStatus);
            domainService.save(domainEntity);
        }
    }

    @Override
    public void addNewCertificateToDomain(Domain domain, OrdersId ordersId, CertificateId certificateId) {
        DomainEntity domainEntity = domainService.findOneByDomain(domain);
        if (domainEntity != null) {
            domainEntity.addNewCertificate(ordersId, certificateId);
            domainService.save(domainEntity);
        }
    }

    private CertificateOrderStatus doAcmGeneration(OrdersEntity it) {
        DomainCertificate certificate = null;
        try {
            certificate = acmeManagerService.generate(it.getLocation(), it.getDomain());
        } catch (Exception e) {
            log.error("can not generate certificate for order {} ", it.getId());
        }
        return Optional.ofNullable(certificate).map(d -> CertificateOrderStatus.GENERATED).orElse(CertificateOrderStatus.FAIL_GENERATING);
    }


    private CertificateOrderStatus doAcmValidation(OrdersEntity order) {
        try {
            Status status = acmeManagerService.validate(order.getLocation().url(), order.getChallengeValidationType().getChallenge());
            return status == Status.VALID ? CertificateOrderStatus.VALIDATED_VALID : CertificateOrderStatus.VALIDATED_INVALID;
        } catch (Exception e) {
            return CertificateOrderStatus.VALIDATED_INVALID;
        }
    }

}
