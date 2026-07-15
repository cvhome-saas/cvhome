package com.asrevo.cvhome.payment.model.payment;

import java.math.BigDecimal;
import java.time.Instant;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

import lombok.Builder;

@Builder
public record PaymentRequest(String ref, BigDecimal amount, CurrencyCode currency, PaymentType paymentType, Instant expireAt,
                             String successUrl, String cancelUrl) {
}