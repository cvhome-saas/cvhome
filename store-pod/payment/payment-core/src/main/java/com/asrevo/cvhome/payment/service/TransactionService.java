package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;

public interface TransactionService {

    Optional<Transaction> findByRefAndStore(String ref, StoreMerchantId storeMerchantId);

    Optional<Transaction> findById(Long id);

    void completeTransaction(Long transactionId, PaymentStatus status);

    Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(Long transactionId, PaymentRequest request, PaymentInitiateResult initiateResult);

    Transaction createCODTransaction(StoreMerchantId store, PaymentRequest request);

    Transaction createManualTransferTransaction(StoreMerchantId store, PaymentRequest request);

    void approvePayment(StoreMerchantId store, Long transactionId, String transactionNo);

    void rejectPayment(StoreMerchantId store, Long transactionId);
}
