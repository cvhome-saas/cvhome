package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookUseCaseHandler {

    private final TransactionService transactionService;
    private final ExternalOrderService externalOrderService;

    public void handleUseCase(WebhookResult result) {
        log.info("Handling use case: {}", result.paymentUseCase());
        switch (result.paymentUseCase()) {
            case PAYMENT_SUCCEEDED, PAYMENT_FAILED -> {
                transactionService.completeTransaction(result.transactionId(), result.status());
                // Propagate to Checkout Service
                transactionService.findById(result.transactionId()).ifPresent(tx -> {
                    PaymentStatus status = result.status();
                    log.info("Propagating payment status {} to checkout for order {} store {}", status, tx.getRef(),
                            tx.getStoreMerchantId());
                    try {
                        externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(), status);
                    } catch (Exception e) {
                        log.error("Failed to propagate payment status to checkout service for order {}", tx.getRef(), e);
                    }
                });
            }
            case UNKNOWN -> log.info("No action for webhook event, transactionId={}", result.transactionId());
            default -> log.warn("Unhandled payment use case: {}", result.paymentUseCase());
        }
    }

}
