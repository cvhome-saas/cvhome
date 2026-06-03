package com.asrevo.cvhome.payment.service.processor;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StripeProcessor implements PaymentProcessor {

    @Override
    public PaymentResponse initiatePayment(PaymentConfiguration config, PaymentRequest request) {
        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setApiKey(config.getSecretKey())
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://example.com/success?orderId=" + request.orderId())
                .setCancelUrl("https://example.com/cancel?orderId=" + request.orderId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.currency().code().toLowerCase())
                                                .setUnitAmount(request.amount().longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + request.orderId())
                                                                .build())
                                                .build())
                                .build())
                .setClientReferenceId(request.orderId().toString())
                .build();

        try {
            Session session = Session.create(params, requestOptions);
            return new PaymentResponse(PaymentStatus.PENDING, session.getUrl());
        } catch (Exception e) {
            log.error("Error creating Stripe session for order {}", request.orderId(), e);
            return new PaymentResponse(PaymentStatus.FAILED);
        }
    }
}

