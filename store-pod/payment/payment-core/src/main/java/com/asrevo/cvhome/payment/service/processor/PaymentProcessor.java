package com.asrevo.cvhome.payment.service.processor;

import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.DefaultPaymentConfig;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

public interface PaymentProcessor {
    PaymentResponse initiatePayment(DefaultPaymentConfig paymentConfig, PaymentSecret secret, PaymentRequest request, Long transactionId);
}
