package com.asrevo.cvhome.payment.service.processor;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.DefaultPaymentConfig;
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
    public PaymentResponse initiatePayment(DefaultPaymentConfig paymentConfig, PaymentSecret secret, PaymentRequest request,
                                           Long transactionId) {
        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setApiKey(secret.getSecretKey())
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.successUrl())
                .setCancelUrl(request.cancelUrl())
                .setExpiresAt(Instant.now()
                        .plusSeconds(paymentConfig.expireIn().toSeconds())
                        .getEpochSecond()
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.currency().code().toLowerCase())
                                                .setUnitAmount(request.amount().longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + request.ref())
                                                                .build())
                                                .build())
                                .build())
                .setClientReferenceId(transactionId.toString())
                .build();

        try {
            Session session = Session.create(params, requestOptions);
            return PaymentResponse.builder()
                    .status(PaymentStatus.PENDING)
                    .redirectUrl(session.getUrl())
                    .isRedirect(true)
                    .externalId(session.getId())
                    .build();
        } catch (Exception e) {
            log.error("Error creating Stripe session for order {}", request.ref(), e);
            return PaymentResponse.failed();
        }
    }
}

