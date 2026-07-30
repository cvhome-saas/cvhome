package com.asrevo.cvhome.payment.service;

import java.util.Optional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

public interface TransactionService {

    String createInitialTransaction(StoreMerchantId store, PaymentRequest request);

    void completeInitiateTransaction(StoreMerchantId store, String transactionInternalRef, PaymentRequest request,
                                     PaymentInitiateResult initiateResult);

    void completeSuccess(StoreMerchantId store, String transactionInternalRef);

    void completeFailed(StoreMerchantId store, String transactionInternalRef);

    void approvePayment(StoreMerchantId store, String internalRef, String transactionNo);

    void rejectPayment(StoreMerchantId store, String internalRef);

    PaymentResponse status(StoreMerchantId store, String ref);

    Optional<PaymentInitiateResult> findExistingInitialResultByRequestRef(StoreMerchantId store, String requestRef);
}
