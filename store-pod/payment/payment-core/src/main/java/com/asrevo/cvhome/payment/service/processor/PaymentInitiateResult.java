package com.asrevo.cvhome.payment.service.processor;

import com.asrevo.cvhome.payment.model.payment.PaymentStatus;

import lombok.Builder;

@Builder
public record PaymentInitiateResult(String redirectUrl, String externalId, PaymentStatus status) {
}
