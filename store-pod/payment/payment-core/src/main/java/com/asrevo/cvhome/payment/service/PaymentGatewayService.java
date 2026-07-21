package com.asrevo.cvhome.payment.service;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
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
    private final ExternalOrderService externalOrderService;

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

        if (PaymentType.MANUAL_TRANSFER.equals(request.paymentType())) {
            return handleManualTransferPayment(store, request);
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

    private PaymentResponse handleManualTransferPayment(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = transactionService.createManualTransferTransaction(store, request);
        return PaymentResponse.builder()
                .status(PaymentStatus.PENDING)
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

    public void submitProof(Long transactionId, String transactionNo, String proofImage) {
        transactionService.submitProof(transactionId, transactionNo, proofImage);
        transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating WAITING_VERIFICATION status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(),
                        com.asrevo.cvhome.store.core.entity.common.PaymentStatus.WAITING_VERIFICATION);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    }

    public void approvePayment(StoreMerchantId store, Long transactionId) {
        transactionService.approvePayment(store, transactionId);
        transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating PAID status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(),
                        com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PAID);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    }

    public void rejectPayment(StoreMerchantId store, Long transactionId, String reason) {
        transactionService.rejectPayment(store, transactionId, reason);
        transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating REJECTED status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(),
                        com.asrevo.cvhome.store.core.entity.common.PaymentStatus.REJECTED);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    }

    private void handleUseCase(WebhookResult result) {
        log.info("Handling use case: {}", result.paymentUseCase());
        switch (result.paymentUseCase()) {
            case PAYMENT_SUCCEEDED, PAYMENT_FAILED -> {
                transactionService.completeTransaction(result.transactionId(), result.status());
                // Propagate to Checkout Service
                transactionService.findById(result.transactionId()).ifPresent(tx -> {
                    com.asrevo.cvhome.store.core.entity.common.PaymentStatus status = mapStatus(result.status());
                    log.info("Propagating payment status {} to checkout for order {} store {}", status, tx.getRef(),
                            tx.getStoreMerchantId());
                    try {
                        externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(), status);
                    } catch (Exception e) {
                        log.error("Failed to propagate payment status to checkout service for order {}", tx.getRef(), e);
                    }
                });
            }
            default -> log.warn("Unknown payment use case: {}", result.paymentUseCase());
        }
    }

    private com.asrevo.cvhome.store.core.entity.common.PaymentStatus mapStatus(PaymentStatus status) {
        return switch (status) {
            case PAID -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PAID;
            case FAILED -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.FAILED;
            case PENDING -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PENDING;
            case PAY_LATER -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PENDING;
            case WAITING_VERIFICATION -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.WAITING_VERIFICATION;
            case REJECTED -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.REJECTED;
            case EXPIRED -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.EXPIRED;
            case CANCELLED -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.CANCELLED;
            case PROCESSING -> com.asrevo.cvhome.store.core.entity.common.PaymentStatus.PROCESSING;
        };
    }

}
