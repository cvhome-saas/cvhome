package com.asrevo.cvhome.payment.model.payment;

public enum PaymentStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    EXPIRED,
    CANCELLED,
    WAITING_VERIFICATION,
    REJECTED,
    PAY_LATER
}