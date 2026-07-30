package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WebhookUseCaseHandler {

    private final TransactionService transactionService;

    public void handleUseCase(StoreMerchantId store,WebhookResult result) {
        log.info("Handling use case: {}", result.paymentUseCase());
        switch (result.paymentUseCase()) {
            case PAYMENT_SUCCEEDED, PAYMENT_FAILED -> transactionService.completeTransaction(store,result.internalReference(), result.status());
            case NONE -> log.info("No action for webhook event, transactionId={}", result.internalReference());
            default -> log.warn("Unhandled payment use case: {}", result.paymentUseCase());
        }
    }

}
/*
*                   @TODO fire event to order service when completeTransaction
*                 // Propagate to Checkout Service
                transactionService.findByTransactionInternalRef(result.internalReference()).ifPresent(tx -> {
                    PaymentStatus status = result.status();
                    log.info("Propagating payment status {} to checkout for order {} store {}", status, tx.getInternalRef(),
                            tx.getStoreMerchantId());
                    try {
                        externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getInternalRef(), status);
                    } catch (Exception e) {
                        log.error("Failed to propagate payment status to checkout service for order {}", tx.getInternalRef(), e);
                    }
                });

*
* */