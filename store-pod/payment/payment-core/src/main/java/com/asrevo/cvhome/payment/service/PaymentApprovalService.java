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

    public void approvePayment(StoreMerchantId store, String internalRef, String transactionNo) {
        transactionService.approvePayment(store, internalRef, transactionNo);
    }

    public void rejectPayment(StoreMerchantId store, String internalRef) {
        transactionService.rejectPayment(store, internalRef);
    }

}
