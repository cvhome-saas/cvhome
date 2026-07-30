package com.asrevo.cvhome.payment.model.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Builder;

@Builder
public record PaymentRequest(String ref, BigDecimal amount, CurrencyCode currency, PaymentType paymentType, Instant expireAt,
                             String successUrl, String cancelUrl) {
    public PaymentRequest {
        if (Objects.isNull(ref)) {
            throw new IllegalArgumentException("Payment request ref invalid");
        }

        if (Objects.isNull(amount)) {
            throw new IllegalArgumentException("Payment request currency invalid");
        }
        if (Objects.isNull(currency)) {
            throw new IllegalArgumentException("Payment request currency invalid");
        }
        if (Objects.isNull(paymentType)) {
            throw new IllegalArgumentException("Payment request paymentType invalid");
        }
        if (Objects.isNull(successUrl)) {
            throw new IllegalArgumentException("Payment request successUrl invalid");
        }
        if (Objects.isNull(cancelUrl)) {
            throw new IllegalArgumentException("Payment request cancelUrl invalid");
        }

    }
}