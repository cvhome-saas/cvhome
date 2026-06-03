package com.asrevo.cvhome.payment.service.processor;

import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

public interface PaymentProcessor{
    PaymentResponse initiatePayment(PaymentConfiguration config, PaymentRequest request);
}
