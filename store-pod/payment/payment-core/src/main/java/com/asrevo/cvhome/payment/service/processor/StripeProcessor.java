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
import com.asrevo.cvhome.payment.service.processor.exception.InvalidWebhookPayload;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
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

    private static <T> T unSafeDeserialization(Event event, Class<T> clazz) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            return clazz.cast(stripeObject);
        } catch (ClassCastException e) {
            throw new InvalidWebhookPayload("Stripe event data object is not of expected type: " + clazz.getSimpleName(), e);
        } catch (EventDataObjectDeserializationException e) {
            throw new InvalidWebhookPayload("Failed to deserialize Stripe event data object", e);
        }
    }

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

    @Override
    public PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request)
            throws FailedPaymentInitiate {
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
                .setClientReferenceId(internalReference)
                .build();

        try {
            Session session = Session.create(params, requestOptions);
            return PaymentInitiateResult.builder()
                    .redirectUrl(session.getUrl())
                    .externalId(session.getId())
                    .status(PaymentStatus.PENDING)
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
            Session session = unSafeDeserialization(event, Session.class);
            String clientReferenceId = session.getClientReferenceId();
            log.info("Processing successful Stripe session for client reference ID: {}", clientReferenceId);
            return WebhookResult.builder()
                    .internalReference(clientReferenceId)
                    .status(PaymentStatus.PAID)
                    .paymentUseCase(PaymentUseCase.PAYMENT_SUCCEEDED)
                    .build();
        } else if ("checkout.session.expired".equals(event.getType()) || "payment_intent.payment_failed".equals(event.getType())) {
            Session session = unSafeDeserialization(event, Session.class);
            String clientReferenceId = session.getClientReferenceId();
            log.info("Processing failed Stripe session for client reference ID: {}", clientReferenceId);
            return WebhookResult.builder()
                    .internalReference(clientReferenceId)
                    .status(PaymentStatus.FAILED)
                    .paymentUseCase(PaymentUseCase.PAYMENT_FAILED)
                    .build();
        }
        return WebhookResult.noneUseCase();
    }

    @Override
    public PaymentType type() {
        return PaymentType.STRIPE;
    }


}

