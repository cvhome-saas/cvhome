package com.asrevo.cvhome.payment.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.payment.service.processor.StripeProcessor;
import com.asrevo.cvhome.payment.service.webhook.StripeWebhook;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationRepository configRepository;
    private final StripeProcessor stripeProcessor;
    private final StripeWebhook stripeWebhook;

    public PaymentResponse initiatePayment(StoreMerchantId store, PaymentRequest request) {
        log.info("Initiating payment for store {} and order {}", store, request.orderId());

        if (PaymentType.COD.equals(request.paymentType())) {
            return new PaymentResponse(PaymentStatus.PAY_LATER);
        } else if (PaymentType.MANUAL_TRANSFER.equals(request.paymentType())) {
            return new PaymentResponse(PaymentStatus.PENDING);
        }

        PaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            return new PaymentResponse(PaymentStatus.FAILED);
        }

        return switch (request.paymentType()) {
            case STRIPE -> stripeProcessor.initiatePayment(config, request);
            default -> new PaymentResponse(PaymentStatus.FAILED);
        };
    }

    private PaymentConfiguration getPaymentConfiguration(StoreMerchantId store, PaymentType paymentType) {
        return configRepository.findByStoreMerchantIdAndPaymentType(store, paymentType)
                .filter(PaymentConfiguration::isEnabled)
                .orElse(null);
    }

    public PaymentResponse status(StoreMerchantId store, Long orderId) {

        return new PaymentResponse(PaymentStatus.PAID);
    }

    public void handleFailedWebhook(Long orderId, String payload, Map<String, Object> headers) {

        PaymentConfiguration config = getPaymentConfigurationByOrderId(orderId);

        if (config == null) {
            log.warn("No enabled payment configuration found for order {}", orderId);
            return;
        }

        switch (config.getPaymentType()) {
            case STRIPE -> stripeWebhook.handleFailedWebhook(orderId,config.getStoreMerchantId(), payload, headers, config);
            default -> throw new IllegalArgumentException("Unsupported payment type for webhook: " + config.getPaymentType());
        }
    }


    public void handleSuccessWebhook(Long orderId, String payload, Map<String, Object> headers) {
        PaymentConfiguration config = getPaymentConfigurationByOrderId(orderId);

        if (config == null) {
            log.warn("No enabled payment configuration found for order {}", orderId);
            return;
        }

        switch (config.getPaymentType()) {
            case STRIPE -> stripeWebhook.handleSuccessWebhook(orderId,config.getStoreMerchantId(), payload, headers, config);
            default -> throw new IllegalArgumentException("Unsupported payment type for webhook: " + config.getPaymentType());
        }

    }


    private PaymentConfiguration getPaymentConfigurationByOrderId(Long orderId) {
        return null;
    }
}
