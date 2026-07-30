package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

public interface TransactionService {

    Optional<Transaction> findByRequestRef(StoreMerchantId store, String ref);

    void completeTransaction(StoreMerchantId store,String transactionInternalRef, PaymentStatus status);

    Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(StoreMerchantId store,String transactionInternalRef, PaymentRequest request, PaymentInitiateResult initiateResult);

    void approvePayment(StoreMerchantId store, String internalRef, String transactionNo);

    void rejectPayment(StoreMerchantId store, String internalRef);
}
