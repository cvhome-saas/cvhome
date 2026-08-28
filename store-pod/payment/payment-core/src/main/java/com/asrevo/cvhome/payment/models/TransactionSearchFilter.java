package com.asrevo.cvhome.payment.models;

import java.time.Instant;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

public record TransactionSearchFilter(
        PaymentStatus status,
        PaymentType paymentType,
        String requestRef,
        String internalRef,
        Instant transactionDateFrom,
        Instant transactionDateTo) {
}
