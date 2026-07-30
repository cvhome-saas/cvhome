package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

public interface TransactionService {

    Optional<Transaction> findByRefAndStore(String ref, StoreMerchantId storeMerchantId);

    Optional<Transaction> findById(Long id);

    Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(Long transactionId, PaymentRequest request, PaymentInitiateResult initiateResult);

    void completeTransaction(Long transactionId, PaymentStatus status);

    void approvePayment(StoreMerchantId store, Long transactionId, String transactionNo);

    void rejectPayment(StoreMerchantId store, Long transactionId);
}
