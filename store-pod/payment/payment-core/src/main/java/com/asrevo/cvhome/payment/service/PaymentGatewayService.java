package com.asrevo.cvhome.payment.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;
import com.asrevo.cvhome.payment.service.processor.PaymentProcessor;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationRepository configRepository;
    private final TransactionService transactionService;

    private final List<PaymentProcessor> paymentProcessors;


    @Transactional
    public PaymentResponse initiatePayment(StoreMerchantId store, PaymentRequest request) {
        log.info("Initiating payment for store {} and order {}", store, request.ref());

        Transaction existingTransaction = transactionService.findByRefAndStore(request.ref(), store).orElse(null);

        if (Objects.nonNull(existingTransaction)) {
            return PaymentResponse.builder()
                    .status(existingTransaction.getStatus())
                    .gatewayRef(existingTransaction.getInternalRef())
                    .build();
        }

        PaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            return PaymentResponse.failed();
        }

        // Initialize transaction
        Transaction transaction = transactionService.createInitialTransaction(store, request);

        try {

            PaymentProcessor processor = getProcessor(request.paymentType()).orElse(null);
            if (processor == null) {
                log.warn("un supported payment type {}", request.paymentType());
                return PaymentResponse.failed();
            }

            PaymentInitiateResult initiateResult = processor.initiate(transaction.getInternalRef(), config, request);

            log.info("Initiating transaction {} with gateway for store {} and type {} to {}", transaction.getInternalRef(), store,
                    request.paymentType(), initiateResult.externalId());
            transactionService.completeInitiateTransaction(transaction.getInternalRef(), request, initiateResult);

            return PaymentResponse.builder()
                    .status(initiateResult.status())
                    .gatewayRef(transaction.getInternalRef())
                    .redirectUrl(transaction.getRedirectUrl())
                    .build();
        } catch (Exception _) {
            return PaymentResponse.failed(transaction.getInternalRef());
        }
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
                        .gatewayRef(tx.getInternalRef())
                        .build())
                .orElse(PaymentResponse.failed());
    }

    public void handleWebhook(StoreMerchantId store, PaymentType paymentType, String payload, Map<String, String> headers) {
        PaymentConfiguration config = getPaymentConfiguration(store, paymentType);
        if (config == null) {
            log.warn("No enabled {} configuration found for store {}", paymentType, store);
            return;
        }

        PaymentProcessor processor = getProcessor(paymentType).orElse(null);
        if (processor == null) {
            log.warn("No enabled {} processor found for store {}", paymentType, store);
            return;
        }

        WebhookResult result = processor.handleWebhook(store, payload, headers, config);
        handleUseCase(result);
    }

    private void handleUseCase(WebhookResult result) {
        log.info("Handling use case: {}", result.paymentUseCase());
        switch (result.paymentUseCase()) {
            case PAYMENT_SUCCEEDED, PAYMENT_FAILED -> transactionService.completeTransaction(result.internalReference(), result.status());
            default -> log.warn("Unknown payment use case: {}", result.paymentUseCase());
        }
    }

    public Optional<PaymentProcessor> getProcessor(PaymentType type) {
        return this.paymentProcessors.stream().filter(p -> p.type() == type).findFirst();
    }
}
