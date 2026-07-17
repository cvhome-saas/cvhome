package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.FailedPaymentInitiate;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidPaymentReferenceId;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidWebhookPayload;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class StripeProcessor implements PaymentProcessor {

    private static Event getEvent(String payload, Map<String, String> headers, PaymentConfiguration configuration)
            throws InvalidWebhookPayload {
        try {
            String sigHeader = headers.get("stripe-signature");
            if (sigHeader == null) {
                sigHeader = headers.get("Stripe-Signature");
            }
            return Webhook.constructEvent(payload, sigHeader, configuration.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.error("Signature verification failed for Stripe webhook", e);
            throw new InvalidWebhookPayload(e.getMessage(), e);
        }
    }

    private static Long getTransactionId(Session session) {
        String transactionIdStr = session.getClientReferenceId();
        if (transactionIdStr == null) {
            throw new InvalidPaymentReferenceId("Transaction ID not found in Stripe session payload");
        }
        try {
            return Long.valueOf(transactionIdStr);
        } catch (NumberFormatException _) {
            throw new InvalidPaymentReferenceId("Transaction ID not found in Stripe session payload");
        }
    }

    @Override
    public PaymentInitiateResult initiate(PaymentSecret secret, PaymentRequest request,
                                          Long transactionId) throws FailedPaymentInitiate {
        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setApiKey(secret.getSecretKey())
                        .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.successUrl())
                .setCancelUrl(request.cancelUrl())
                .setExpiresAt(request.expireAt().getEpochSecond())
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
                .putMetadata("transactionId", transactionId.toString())
                .putMetadata("ref", request.ref())
                .build();

        try {
            Session session = Session.create(params, requestOptions);
            return PaymentInitiateResult.builder()
                    .redirectUrl(session.getUrl())
                    .externalId(session.getId())
                    .build();

        } catch (StripeException e) {
            throw new FailedPaymentInitiate(e.getMessage(), e);
        }

    }

    @Override
    public WebhookResult handleWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                       PaymentConfiguration configuration) throws InvalidWebhookPayload {
        log.info("Handling Stripe webhook for store {}", storeMerchantId);
        Event event = getEvent(payload, headers, configuration);

        log.info("Stripe webhook event type: {} version {}", event.getType(), event.getApiVersion());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
            Long transactionId = getTransactionId(session);
            log.info("Processing successful Stripe session for transaction ID: {}", transactionId);
            return WebhookResult.builder()
                    .transactionId(transactionId)
                    .status(PaymentStatus.PAID)
                    .paymentUseCase(PaymentUseCase.PAYMENT_SUCCEEDED)
                    .build();
        } else if ("checkout.session.expired".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
            Long transactionId = getTransactionId(session);
            log.info("Processing failed Stripe session for transaction ID: {}", transactionId);
            return WebhookResult.builder()
                    .transactionId(transactionId)
                    .status(PaymentStatus.FAILED)
                    .paymentUseCase(PaymentUseCase.PAYMENT_FAILED)
                    .build();
        }
        return WebhookResult.builder().build();
    }


}

