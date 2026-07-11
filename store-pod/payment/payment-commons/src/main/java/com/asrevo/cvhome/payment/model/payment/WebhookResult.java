package com.asrevo.cvhome.payment.model.payment;

import lombok.Builder;

@Builder
public record WebhookResult(Long transactionId, PaymentStatus status) {
}
