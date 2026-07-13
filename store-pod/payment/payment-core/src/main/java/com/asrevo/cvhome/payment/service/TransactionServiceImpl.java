package com.asrevo.cvhome.payment.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;

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
    @Transactional(readOnly = true)
    public Optional<Transaction> findByRefAndStore(String ref, StoreMerchantId storeMerchantId) {
        return transactionRepository.findTopByRequestRefAndStoreMerchantIdOrderByTransactionDateDesc(ref, storeMerchantId);
    }

    @Override
    public Optional<Transaction> findByTransactionInternalRef(String transactionInternalRef) {
        return transactionRepository.findByInternalRef(transactionInternalRef);
    }

    @Override
    @Transactional
    public void completeTransaction(String transactionInternalRef, PaymentStatus status) {
        Transaction transaction = findByTransactionInternalRef(transactionInternalRef)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionInternalRef));
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = constructTransaction(store, request);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void completeInitiateTransaction(String transactionInternalRef, PaymentRequest request, PaymentInitiateResult initiateResult) {
        this.findByTransactionInternalRef(transactionInternalRef).ifPresent(transaction -> {
            transaction.setPaymentGatewayExternalId(initiateResult.externalId());
            transaction.setRedirectUrl(initiateResult.redirectUrl());
            transaction.setStatus(initiateResult.status());
            transaction.setSuccessUrl(request.successUrl());
            transaction.setCancelUrl(request.cancelUrl());
            transactionRepository.save(transaction);
        });

    }

}
