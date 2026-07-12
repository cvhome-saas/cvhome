package com.asrevo.cvhome.payment.service;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;
import com.asrevo.cvhome.payment.service.processor.StripeProcessor;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationRepository configRepository;
    private final TransactionService transactionService;
    private final StripeProcessor stripeProcessor;

    @Transactional
    public PaymentResponse initiatePayment(StoreMerchantId store, PaymentRequest request) {
        log.info("Initiating payment for store {} and order {}", store, request.ref());

        Transaction existingTransaction = transactionService.findByRefAndStore(request.ref(), store).orElse(null);

        if (Objects.nonNull(existingTransaction)) {
            return PaymentResponse.builder()
                    .status(existingTransaction.getStatus())
                    .transactionId(existingTransaction.getId())
                    .build();
        }

        if (PaymentType.COD.equals(request.paymentType())) {
            return handleCODPayment(store, request);
        }

        PaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            return PaymentResponse.failed();
        }

        // Initialize transaction
        Transaction transaction = transactionService.createInitialTransaction(store, request);

        try {
            PaymentInitiateResult initiateResult = switch (request.paymentType()) {
                case STRIPE -> stripeProcessor.initiate(config, request, transaction.getId());
                default -> throw new IllegalArgumentException("Unsupported payment type: " + request.paymentType());
            };
            transactionService.completeInitiateTransaction(transaction.getId(), request, initiateResult);

            return PaymentResponse.builder()
                    .status(PaymentStatus.PENDING)
                    .transactionId(transaction.getId())
                    .redirectUrl(transaction.getRedirectUrl())
                    .build();
        } catch (Exception _) {
            return PaymentResponse.failed(transaction.getId());
        }
    }


    private PaymentResponse handleCODPayment(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = transactionService.createCODTransaction(store, request);
        return PaymentResponse.builder()
                .status(PaymentStatus.PAY_LATER)
                .transactionId(transaction.getId())
                .build();
    }


    private PaymentConfiguration getPaymentConfiguration(StoreMerchantId store, PaymentType paymentType) {
        return configRepository.findByStoreMerchantIdAndPaymentType(store, paymentType)
                .filter(PaymentConfiguration::isEnabled)
                .orElse(null);
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
        PaymentConfiguration config = getPaymentConfiguration(store, paymentType);
        if (config == null) {
            log.warn("No enabled {} configuration found for store {}", paymentType, store);
            return;
        }

        try {
            switch (paymentType) {
                case STRIPE -> {
                    WebhookResult result = stripeProcessor.handleWebhook(store, payload, headers, config);
                    handleUseCase(result);
                }
                default -> log.warn("Unsupported payment type for webhook: {}", paymentType);
            }

        } catch (Exception e) {
            log.error("Error processing {} webhook for store {}", paymentType, store, e);
        }
    }

    private void handleUseCase(WebhookResult result) {
        log.info("Handling use case: {}", result.paymentUseCase());
        switch (result.paymentUseCase()) {
            case PAYMENT_SUCCEEDED, PAYMENT_FAILED -> transactionService.completeTransaction(result.transactionId(), result.status());
            default -> log.warn("Unknown payment use case: {}", result.paymentUseCase());
        }
    }

}
