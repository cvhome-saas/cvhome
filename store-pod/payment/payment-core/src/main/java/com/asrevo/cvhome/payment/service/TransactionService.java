package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.ReadableTransactionList;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;

public interface TransactionService {

    String createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(StoreMerchantId store, String transactionInternalRef, PaymentRequest request,
                                     PaymentInitiateResult initiateResult);

    void completeSuccess(StoreMerchantId store, String transactionInternalRef);

    void completeFailed(StoreMerchantId store, String transactionInternalRef);

    void completeCanceled(StoreMerchantId store, String transactionInternalRef);

    void approvePayment(StoreMerchantId store, String internalRef, String transactionNo);

    void rejectPayment(StoreMerchantId store, String internalRef);

    ReadableTransactionList list(StoreMerchantId store, TransactionSearchFilter filter, Pageable pageable);

    PaymentResponse status(StoreMerchantId store, String ref);

    Optional<PaymentInitiateResult> findExistingInitialResultByRequestRef(StoreMerchantId store, String requestRef);
}
