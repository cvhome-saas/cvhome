package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentApprovalService {

    private final TransactionService transactionService;

    /* @TODO should fire event
        private final ExternalOrderService externalOrderService;
            transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating PAID status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(), PaymentStatus.PAID);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    */

    public void approvePayment(StoreMerchantId store, String internalRef, String transactionNo) {
        transactionService.approvePayment(store, internalRef, transactionNo);
    }

    public void rejectPayment(StoreMerchantId store, String internalRef) {
        transactionService.rejectPayment(store, internalRef);
    }

}
