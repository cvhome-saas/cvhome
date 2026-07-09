package com.asrevo.cvhome.payment.model.payment;

import java.math.BigDecimal;
import java.time.Instant;

import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;

public record PaymentRequest(Long orderId, BigDecimal amount, CurrencyCode currency, PaymentType paymentType, Instant expireAt) {

}