package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.catalog.services.product.model.PaymentRequest;
import com.asrevo.cvhome.catalog.services.product.model.PaymentResponse;

@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {

    @PostMapping("/payments/initiate")
    PaymentResponse initiatePayment(@RequestBody PaymentRequest paymentRequest);


    @PostMapping("/payments/{orderId}/status")
    PaymentResponse status(@PathVariable("orderId") Long orderId);


}