package com.asrevo.cvhome.payment.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private static Transaction constructTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setInternalRef(UUID.randomUUID().toString());
        transaction.setRequestRef(request.ref());
        transaction.setStoreMerchantId(store);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setPaymentType(request.paymentType());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setTransactionDate(Instant.now());
        transaction.setExpireAt(request.expireAt());
        return transaction;
    }

    @Override
    @Transactional
    public void completeSuccess(StoreMerchantId store, String transactionInternalRef) {
        Transaction transaction = getTransaction(store, transactionInternalRef).completeSuccess();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void completeFailed(StoreMerchantId store, String transactionInternalRef) {
        Transaction transaction = getTransaction(store, transactionInternalRef).completeFailed();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public String createInitialTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = transactionRepository.save(constructTransaction(store, request));
        return transaction.getInternalRef();
    }

    @Override
    @Transactional
    public void completeInitiateTransaction(StoreMerchantId store, String transactionInternalRef, PaymentRequest request,
                                            PaymentInitiateResult initiateResult) {
        transactionRepository.findByStoreMerchantIdAndInternalRef(store, transactionInternalRef).ifPresent(transaction -> {
            transaction.setPaymentGatewayExternalId(initiateResult.externalId());
            transaction.setRedirectUrl(initiateResult.redirectUrl());
            transaction.setStatus(toTransactionStatus(initiateResult.status()));
            transaction.setSuccessUrl(request.successUrl());
            transaction.setCancelUrl(request.cancelUrl());
            transactionRepository.save(transaction);
        });

    }

    private static PaymentStatus toTransactionStatus(PaymentInitiateStatus status) {
        return switch (status) {
            case PENDING -> PaymentStatus.PENDING;
            case FAILED -> PaymentStatus.FAILED;
            case PAID -> PaymentStatus.PAID;
        };
    }

    @Override
    @Transactional
    public void approvePayment(StoreMerchantId store, String internalRef, String transactionNo) {
        Transaction transaction = getTransaction(store, internalRef);
        transaction.setTransactionNo(transactionNo);
        transaction.setStatus(PaymentStatus.PAID);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void rejectPayment(StoreMerchantId store, String internalRef) {
        Transaction transaction = getTransaction(store, internalRef);
        transaction.setStatus(PaymentStatus.REJECTED);
        transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    @Override
    public PaymentResponse status(StoreMerchantId store, String requestRef) {
        return this.transactionRepository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(store, requestRef)
                .map(tx -> PaymentResponse.builder()
                        .status(tx.getStatus())
                        .redirectUrl(tx.getRedirectUrl())
                        .gatewayRef(tx.getInternalRef())
                        .build())
                .orElse(PaymentResponse.failed());
    }

    @Override
    public Optional<PaymentInitiateResult> findExistingInitialResultByRequestRef(StoreMerchantId store, String requestRef) {
        return this.transactionRepository.findTopByStoreMerchantIdAndRequestRefOrderByTransactionDateDesc(store, requestRef)
                .map(it -> PaymentInitiateResult.builder()
                        .status(toInitiateStatus(it.getStatus()))
                        .gatewayRef(it.getInternalRef())
                        .build());
    }


    private static PaymentInitiateStatus toInitiateStatus(PaymentStatus status) {
        return switch (status) {
            case PAID -> PaymentInitiateStatus.PAID;
            case PENDING, PROCESSING, WAITING_VERIFICATION, AUTHORIZED -> PaymentInitiateStatus.PENDING;
            case FAILED, EXPIRED, CANCELLED, REJECTED, REFUNDED -> PaymentInitiateStatus.FAILED;
        };
    }

    private @NonNull Transaction getTransaction(StoreMerchantId store, String internalRef) {
        return transactionRepository.findByStoreMerchantIdAndInternalRef(store, internalRef)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + internalRef));
    }

}
