package com.asrevo.cvhome.payment.model.payment;

public record PaymentResponse(
        PaymentStatus status,
        String redirectUrl,
        boolean isRedirect) {
    public PaymentResponse(PaymentStatus status) {
        this(status, null, false);
    }

    public PaymentResponse(PaymentStatus status, String redirectUrl) {
        this(status, redirectUrl, true);
    }
}