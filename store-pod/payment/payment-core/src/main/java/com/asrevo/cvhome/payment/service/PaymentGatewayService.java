package com.asrevo.cvhome.payment.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.DefaultPaymentConfig;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.payment.service.processor.StripeProcessor;
import com.asrevo.cvhome.payment.service.webhook.StripeWebhook;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentGatewayService {

    private final PaymentConfigurationRepository configRepository;
    private final TransactionRepository transactionRepository;
    private final StripeProcessor stripeProcessor;
    private final StripeWebhook stripeWebhook;
    private final DefaultPaymentConfig paymentConfig;

    @Transactional
    public PaymentResponse initiatePayment(StoreMerchantId store, PaymentRequest request) {
        log.info("Initiating payment for store {} and order {}", store, request.ref());

        // Check if there is already a transaction for this order
        Optional<Transaction> existingTransaction =
                transactionRepository.findTopByRefAndStoreMerchantIdOrderByTransactionDateDesc(request.ref(), store);

        if (existingTransaction.isPresent()) {
            Transaction tx = existingTransaction.get();
            if (tx.getStatus() == PaymentStatus.PAID || tx.getStatus() == PaymentStatus.PAY_LATER) {
                return PaymentResponse.builder()
                        .status(tx.getStatus())
                        .externalId(tx.getExternalId())
                        .build();
            }
            if (tx.getStatus() == PaymentStatus.PENDING) {
                // If it's not expired, return it
                if (tx.getExpireAt() == null || tx.getExpireAt().isAfter(Instant.now())) {
                    return PaymentResponse.builder()
                            .status(tx.getStatus())
                            .redirectUrl(tx.getRedirectUrl())
                            .isRedirect(tx.getRedirectUrl() != null)
                            .externalId(tx.getExternalId())
                            .build();
                }
                // If expired, mark it as FAILED and allow new attempt
                tx.setStatus(PaymentStatus.FAILED);
                transactionRepository.save(tx);
            }
        }

        if (PaymentType.COD.equals(request.paymentType())) {
            return handleCODPayment(store, request, existingTransaction.orElse(null));
        }

        PaymentConfiguration config = getPaymentConfiguration(store, request.paymentType());

        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", store, request.paymentType());
            // If we have an existing transaction, mark it as FAILED if we were going to use it
            existingTransaction.ifPresent(tx -> {
                tx.setStatus(PaymentStatus.FAILED);
                transactionRepository.save(tx);
            });
            return PaymentResponse.failed();
        }

        // Initialize transaction
        Transaction transaction = createInitialTransaction(store, request, existingTransaction.orElse(null));

        PaymentResponse response = switch (request.paymentType()) {
            case STRIPE -> stripeProcessor.initiatePayment(paymentConfig, config, request, transaction.getId());
            default -> PaymentResponse.failed();
        };

        if (response.status() != PaymentStatus.FAILED) {
            transaction.setExternalId(response.externalId());
            transaction.setRedirectUrl(response.redirectUrl());
            transaction.setStatus(response.status());
            transaction.setSuccessUrl(request.successUrl());
            transaction.setCancelUrl(request.cancelUrl());
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
        }
        transactionRepository.save(transaction);

        return response;
    }

    private PaymentResponse handleCODPayment(StoreMerchantId store, PaymentRequest request, Transaction existing) {
        Transaction transaction = createInitialTransaction(store, request, existing);
        transaction.setStatus(PaymentStatus.PAY_LATER);
        transaction.setTransactionType(TransactionType.AUTHORIZECAPTURE);
        transactionRepository.save(transaction);
        return PaymentResponse.builder()
                .status(PaymentStatus.PAY_LATER)
                .build();
    }

    private Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request, Transaction existing) {
        Transaction transaction = (existing != null) ? existing : new Transaction();
        transaction.setRef(request.ref());
        transaction.setStoreMerchantId(store);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency().code());
        transaction.setPaymentType(request.paymentType());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setTransactionDate(Instant.now());
        transaction.setTransactionType(TransactionType.INIT);
        transaction.setExpireAt(request.expireAt());
        if (existing != null) {
            transaction.setRetryCount(existing.getRetryCount() + 1);
        } else {
            transaction.setRetryCount(0);
        }
        return transactionRepository.save(transaction);
    }

    private PaymentConfiguration getPaymentConfiguration(StoreMerchantId store, PaymentType paymentType) {
        return configRepository.findByStoreMerchantIdAndPaymentType(store, paymentType)
                .filter(PaymentConfiguration::isEnabled)
                .orElse(null);
    }

    public PaymentResponse status(StoreMerchantId store, String ref) {
        return transactionRepository.findTopByRefAndStoreMerchantIdOrderByTransactionDateDesc(ref, store)
                .map(tx -> PaymentResponse.builder()
                        .status(tx.getStatus())
                        .redirectUrl(tx.getRedirectUrl())
                        .isRedirect(tx.getRedirectUrl() != null)
                        .externalId(tx.getExternalId())
                        .build())
                .orElse(PaymentResponse.failed());
    }

    @Transactional
    public void handleFailedWebhook(Long transactionId, String payload, Map<String, Object> headers) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        PaymentConfiguration config = getPaymentConfiguration(tx.getStoreMerchantId(), tx.getPaymentType());
        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", tx.getStoreMerchantId(), tx.getPaymentType());
            return;
        }

        updateTransactionStatus(tx, PaymentStatus.FAILED);

        switch (tx.getPaymentType()) {
            case STRIPE -> stripeWebhook.handleFailedWebhook(tx.getId(), tx.getStoreMerchantId(), payload, headers, config);
            default -> log.warn("Unsupported payment type for webhook: {}", tx.getPaymentType());
        }
    }


    @Transactional
    public void handleSuccessWebhook(Long transactionId, String payload, Map<String, Object> headers) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        PaymentConfiguration config = getPaymentConfiguration(tx.getStoreMerchantId(), tx.getPaymentType());
        if (config == null) {
            log.warn("No enabled payment configuration found for store {} and type {}", tx.getStoreMerchantId(), tx.getPaymentType());
            return;
        }

        updateTransactionStatus(tx, PaymentStatus.PAID);

        switch (tx.getPaymentType()) {
            case STRIPE -> stripeWebhook.handleSuccessWebhook(tx.getId(), tx.getStoreMerchantId(), payload, headers, config);
            default -> log.warn("Unsupported payment type for webhook: {}", tx.getPaymentType());
        }

    }

    private void updateTransactionStatus(Transaction tx, PaymentStatus status) {
        tx.setStatus(status);
        if (status == PaymentStatus.PAID) {
            tx.setTransactionType(TransactionType.CAPTURE);
        }
        transactionRepository.save(tx);
    }

}
