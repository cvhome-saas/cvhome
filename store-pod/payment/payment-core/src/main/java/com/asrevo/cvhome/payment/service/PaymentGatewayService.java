package com.asrevo.cvhome.payment.service;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.payment.service.processor.CODProcessor;
import com.asrevo.cvhome.payment.service.processor.ManualTransferredProcessor;
import com.asrevo.cvhome.payment.service.processor.StripeProcessor;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationService paymentConfigurationService;
    private final TransactionService transactionService;
    private final StripeProcessor stripeProcessor;
    private final CODProcessor codProcessor;
    private final ManualTransferredProcessor manualTransferredProcessor;
    private final WebhookUseCaseHandler webhookUseCaseHandler;

    @Transactional
    public PaymentInitiateResult initiatePayment(StoreMerchantId store, PaymentRequest request) {
        log.info("Initiating payment for store {} and order {}", store, request.ref());

        Transaction existingTransaction = transactionService.findByRefAndStore(request.ref(), store).orElse(null);

        if (Objects.nonNull(existingTransaction)) {
            return PaymentInitiateResult.builder()
                    .status(toInitiateStatus(existingTransaction.getStatus()))
                    .transactionId(existingTransaction.getId())
                    .redirectUrl(existingTransaction.getRedirectUrl())
                    .build();
        }


        ReadablePaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            return PaymentInitiateResult.failed();
        }

        // Initialize transaction
        Transaction transaction = transactionService.createInitialTransaction(store, request);

        try {
            PaymentInitiateResult initiateResult = switch (request.paymentType()) {
                case STRIPE -> stripeProcessor.initiate(config, request, transaction.getId());
                case COD -> codProcessor.initiate(config, request, transaction.getId());
                case MANUAL_TRANSFER -> manualTransferredProcessor.initiate(config, request, transaction.getId());
                default -> throw new IllegalArgumentException("Unsupported payment type: " + request.paymentType());
            };
            transactionService.completeInitiateTransaction(transaction.getId(), request, initiateResult);

            return initiateResult;
        } catch (Exception _) {
            return PaymentInitiateResult.failed(transaction.getId());
        }
    }

    private ReadablePaymentConfiguration getPaymentConfiguration(StoreMerchantId store, PaymentType paymentType) {
        return paymentConfigurationService.getConfig(store, paymentType)
                .filter(ReadablePaymentConfiguration::isEnabled)
                .orElse(null);
    }

    private static PaymentInitiateStatus toInitiateStatus(PaymentStatus status) {
        return switch (status) {
            case PAID -> PaymentInitiateStatus.PAID;
            case PENDING, PROCESSING, WAITING_VERIFICATION, AUTHORIZED -> PaymentInitiateStatus.PENDING;
            case FAILED, EXPIRED, CANCELLED, REJECTED, REFUNDED -> PaymentInitiateStatus.FAILED;
        };
    }

    public PaymentResponse status(StoreMerchantId store, String ref) {
        return transactionService.findByRefAndStore(ref, store)
                .map(tx -> PaymentResponse.builder()
                        .status(tx.getStatus())
                        .redirectUrl(tx.getRedirectUrl())
                        .transactionId(tx.getId())
                        .build())
                .orElse(PaymentResponse.failed());
    }

    public void handleWebhook(StoreMerchantId store, PaymentType paymentType, String payload, Map<String, String> headers) {
        PaymentSecret config = getPaymentConfiguration(store, paymentType);
        if (config == null) {
            log.warn("No enabled {} configuration found for store {}", paymentType, store);
            return;
        }

        try {
            switch (paymentType) {
                case STRIPE -> {
                    WebhookResult result = stripeProcessor.parseWebhook(store, payload, headers, config);
                    webhookUseCaseHandler.handleUseCase(result);
                }
                default -> log.warn("Unsupported payment type for webhook: {}", paymentType);
            }

        } catch (Exception e) {
            log.error("Error processing {} webhook for store {}", paymentType, store, e);
        }
    }

}
