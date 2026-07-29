package com.asrevo.cvhome.payment.service;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentApprovalService {

    private final TransactionService transactionService;
    private final ExternalOrderService externalOrderService;

    public void approvePayment(StoreMerchantId store, Long transactionId, String transactionNo) {
        transactionService.approvePayment(store, transactionId, transactionNo);
        transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating PAID status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(), PaymentStatus.PAID);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    }

    public void rejectPayment(StoreMerchantId store, Long transactionId) {
        transactionService.rejectPayment(store, transactionId);
        transactionService.findById(transactionId).ifPresent(tx -> {
            log.info("Propagating REJECTED status to checkout for order {} store {}", tx.getRef(),
                    tx.getStoreMerchantId());
            try {
                externalOrderService.updatePaymentStatus(tx.getStoreMerchantId(), tx.getRef(), PaymentStatus.REJECTED);
            } catch (Exception e) {
                log.error("Failed to propagate status to checkout service for order {}", tx.getRef(), e);
            }
        });
    }

}
