package com.asrevo.cvhome.payment.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.Transaction;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.repository.payment.TransactionRepository;
import com.asrevo.cvhome.payment.service.processor.PaymentInitiateResult;
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
    public Transaction createInitialTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = new Transaction();
        transaction.setRef(request.ref());
        transaction.setStoreMerchantId(store);
        transaction.setAmount(request.amount());
        transaction.setCurrency(request.currency());
        transaction.setPaymentType(request.paymentType());
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
            transaction.setStatus(PaymentStatus.PENDING);
            transaction.setSuccessUrl(request.successUrl());
            transaction.setCancelUrl(request.cancelUrl());
            transactionRepository.save(transaction);
        });

    }

    @Override
    @Transactional
    public Transaction createCODTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = createInitialTransaction(store, request);
        transaction.setStatus(PaymentStatus.PAY_LATER);
        transaction.setTransactionType(TransactionType.AUTHORIZECAPTURE);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public Transaction createManualTransferTransaction(StoreMerchantId store, PaymentRequest request) {
        Transaction transaction = createInitialTransaction(store, request);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setTransactionType(TransactionType.AUTHORIZECAPTURE);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void submitProof(Long transactionId, String transactionNo, String proofImage) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setTransactionNo(transactionNo);
        transaction.setProofImage(proofImage);
        transaction.setStatus(PaymentStatus.WAITING_VERIFICATION);
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void approvePayment(StoreMerchantId store, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setStatus(PaymentStatus.PAID);
        transaction.setTransactionType(TransactionType.CAPTURE);
        transaction.setVerifiedAt(Instant.now());
        // verifiedBy should be set from security context, I'll skip it for now or use a placeholder
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void rejectPayment(StoreMerchantId store, Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        transaction.setStatus(PaymentStatus.REJECTED);
        transaction.setRejectionReason(reason);
        transactionRepository.save(transaction);
    }

}
