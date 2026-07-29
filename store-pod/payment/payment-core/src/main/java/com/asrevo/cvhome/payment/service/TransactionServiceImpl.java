package com.asrevo.cvhome.payment.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.TransactionType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> findByRefAndStore(String ref, StoreMerchantId storeMerchantId) {
        return transactionRepository.findTopByRefAndStoreMerchantIdOrderByTransactionDateDesc(ref, storeMerchantId);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    @Transactional
    public void completeTransaction(Long transactionId, PaymentStatus status) {
        Transaction transaction = findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setStatus(status);
        if (status == PaymentStatus.PAID) {
            transaction.setTransactionType(TransactionType.CAPTURE);
        }
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction createInitialTransaction(StoreMerchantId store, PaymentConfiguration config, PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setRef(request.ref());
        transaction.setStoreMerchantId(store);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setPaymentType(config.getPaymentType());
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setTransactionDate(Instant.now());
        transaction.setTransactionType(TransactionType.INIT);
        transaction.setExpireAt(request.expireAt());
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void completeInitiateTransaction(Long transactionId, PaymentRequest request, PaymentInitiateResult initiateResult) {
        this.findById(transactionId).ifPresent(transaction -> {
            transaction.setExternalId(initiateResult.externalId());
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
    public void approvePayment(StoreMerchantId store, Long transactionId, String transactionNo) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setTransactionNo(transactionNo);
        transaction.setStatus(PaymentStatus.PAID);
        transaction.setTransactionType(TransactionType.CAPTURE);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void rejectPayment(StoreMerchantId store, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setStatus(PaymentStatus.REJECTED);
        transactionRepository.save(transaction);
    }

}
