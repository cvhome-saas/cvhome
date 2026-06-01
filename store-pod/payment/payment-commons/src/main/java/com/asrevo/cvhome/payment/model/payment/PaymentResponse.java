package com.asrevo.cvhome.payment.model.payment;

public record PaymentResponse(
        PaymentStatus status,
        String redirectUrl,
        boolean isRedirect) {
}