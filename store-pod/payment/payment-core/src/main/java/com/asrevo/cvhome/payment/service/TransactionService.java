package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;

public interface TransactionService {

    Optional<Transaction> findByRefAndStore(String ref, StoreMerchantId storeMerchantId);

    Optional<Transaction> findByTransactionInternalRef(String transactionInternalRef);

    void completeTransaction(String transactionInternalRef, PaymentStatus status);

    Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(String transactionInternalRef, PaymentRequest request, PaymentInitiateResult initiateResult);

}
