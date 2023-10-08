package com.asrevo.cvhome.certificatemanager.service.impl;

import com.asrevo.cvhome.certificatemanager.commons.command.order.GenerateCertificateCommand;
import com.asrevo.cvhome.certificatemanager.commons.command.order.RequestOrderCertificateCommand;
import com.asrevo.cvhome.certificatemanager.commons.command.order.ValidateOrderCommand;
import com.asrevo.cvhome.certificatemanager.commons.domain.*;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersCreateRequestDto;
import com.asrevo.cvhome.certificatemanager.commons.dto.OrdersCreateResponseDto;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.challenges.Challenges;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.certificatemanager.mappers.OrdersMappers;
import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.OrdersService;
import com.asrevo.cvhome.commons.command.CommandPublisher;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Status;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Set;

@Service
@Lazy
@AllArgsConstructor
@Slf4j
public class AcmCertificateOrderServiceImpl implements AcmCertificateOrderService {

    private final OrdersService ordersService;

    private final AcmeManagerService acmeManagerService;

    private final OrdersMappers ordersMappers;

    private final CommandPublisher commandPublisher;


    @Override
    public OrdersCreateResponseDto initiate(OrdersCreateRequestDto createRequest) {
        return this.initiate(createRequest.getDomain(), createRequest.getChallengeValidationType());
    }

    @Override
    public OrdersCreateResponseDto initiate(Domain domain, ChallengeValidationType challengeValidationType) {
        OrdersEntity certificateOrder = OrdersEntity.createOrder(domain, challengeValidationType);
        OrdersEntity savedOrder = ordersService.save(certificateOrder);
        RequestOrderCertificateCommand command = new RequestOrderCertificateCommand();
        command.setId(savedOrder.getId());
        commandPublisher.publish(command);
        return ordersMappers.toOrdersCreateResponse(savedOrder);
    }

    @Transactional
    @Override
    public void order(OrdersId orderId) {
        Set<CertificateOrderStatus> preOrderNeededStatus = Set.of(CertificateOrderStatus.INITIATED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preOrderNeededStatus.contains(it.getCertificateOrderStatus())) {
                try {
                    Order o = acmeManagerService.order(it.getDomain());
                    it.requestOrder(new OrderLocation(o.getLocation()), new Challenges(o));
                    ordersService.save(it);
                    this.prepareOrderValidation(orderId);
                } catch (Exception e) {
                    log.error("error when requesting order for order {}", orderId);
                }
            } else {
                log.warn("request to do order certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void validate(OrdersId orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(CertificateOrderStatus.VALIDATION_REQUESTED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                boolean isValid = it.getChallenges().validate(it.getChallengeValidationType());
                if (isValid) {
                    CertificateOrderStatus certificateOrderStatus = doAcmValidation(it);
                    log.info("acmValidation validation is {} for domain {}", certificateOrderStatus, orderId);
                    it.validated(certificateOrderStatus);
                    GenerateCertificateCommand command = new GenerateCertificateCommand();
                    command.setOrdersId(orderId);
                    commandPublisher.publish(command);
                } else {
                    log.warn("performPreValidation validation is {} for domain {}", CertificateOrderStatus.PRE_VALIDATED_INVALID, orderId);
                    it.validated(CertificateOrderStatus.PRE_VALIDATED_INVALID);
                }
                ordersService.save(it);
            } else {
                log.warn("request to do validation certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    @Override
    public void triggerValidate(OrdersId orderId, String type) {
        if (ChallengeValidationType.valueOf(type).isAutomaticValidation()) {
            throw new UnsupportedOperationException("automatic validation not support request validate operation");
        }
        Set<CertificateOrderStatus> preAskValidateNeededStatus = Set.of(CertificateOrderStatus.REQUESTED, CertificateOrderStatus.PRE_VALIDATED_INVALID);
        ordersService.findOneById(orderId).map(it -> {
            if (preAskValidateNeededStatus.contains(it.getCertificateOrderStatus())) {
                it.requestValidate();
                ValidateOrderCommand command = new ValidateOrderCommand();
                command.setId(it.getId());
                OrdersEntity saved = ordersService.save(it);
                commandPublisher.publish(command);
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
    public void prepareOrderValidation(OrdersId orderId) {
        Set<CertificateOrderStatus> preValidationNeededStatus = Set.of(CertificateOrderStatus.REQUESTED);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preValidationNeededStatus.contains(it.getCertificateOrderStatus())) {
                try {
                    log.info("will prepareOrderValidation for order {}", orderId);
                    acmeManagerService.doSetupBeforeValidation(it.getChallenges().getChallenge(it.getChallengeValidationType()));
                    it.requestValidate();
                    ValidateOrderCommand command = new ValidateOrderCommand();
                    command.setId(it.getId());
                    ordersService.save(it);
                    commandPublisher.publish(command);
                } catch (IOException e) {
                    log.error("error when prepareOrderValidation for order {}", orderId);
                }
            } else {
                log.warn("request to do generate Validation Challenge certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });

    }

    @Override
    public void doGeneration(OrdersId orderId) {
        Set<CertificateOrderStatus> preGenerationNeededStatus = Set.of(CertificateOrderStatus.VALIDATED_VALID);
        ordersService.findOneById(orderId).ifPresent(it -> {
            if (preGenerationNeededStatus.contains(it.getCertificateOrderStatus())) {
                log.info("will generate acm certificate for order {}", orderId);
                it.generateOrderCertificate(doAcmGeneration(it));
                ordersService.save(it);
                if (!CertificateOrderStatus.GENERATED.equals(it.getCertificateOrderStatus())) {
                    log.error("generation failed for order {}", orderId);
                }

            } else {
                log.warn("request to do generate certificate for status {} and orderId {}", it.getCertificateOrderStatus(), orderId);
            }
        });
    }

    private DomainCertificate doAcmGeneration(OrdersEntity it) {
        try {
            return acmeManagerService.generate(it.getLocation(), it.getDomain());
        } catch (Exception e) {
            log.error("can not generate certificate for order {} ", it.getId());
        }
        return null;
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
