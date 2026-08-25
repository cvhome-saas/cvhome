package com.asrevo.cvhome.payment.model.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Builder;

@Builder
public record ReadableTransaction(
        Long id,
        String internalRef,
        String requestRef,
        BigDecimal amount,
        CurrencyCode currency,
        PaymentType paymentType,
        PaymentStatus status,
        Instant transactionDate,
        String transactionNo) implements Serializable {
}
