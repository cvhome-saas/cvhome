package com.asrevo.cvhome.payment.services.payment;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;

@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {

    @PostExchange("/payments/initiate")
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest paymentRequest);


    @GetExchange("/payments/{ref}/status")
    PaymentResponse status(StoreMerchantId store, @PathVariable("ref") String ref);


}