package com.asrevo.cvhome.payment.services.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {

    @PostMapping("/payments/initiate")
    PaymentResponse initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest paymentRequest);


    @GetMapping("/payments/{orderId}/status")
    PaymentResponse status(StoreMerchantId store, @PathVariable("orderId") Long orderId);


}