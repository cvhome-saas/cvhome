package com.asrevo.cvhome.payment.model.payment;

import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

import lombok.Builder;

@Builder
public record WebhookResult(Long transactionId, PaymentStatus status, PaymentUseCase paymentUseCase) {
}

