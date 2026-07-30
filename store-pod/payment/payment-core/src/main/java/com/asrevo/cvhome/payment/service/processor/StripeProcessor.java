package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.FailedPaymentInitiate;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidWebhookPayload;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
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

    private static final String INTERNAL_REFERENCE_METADATA_KEY = "internal_reference";

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

    private static Event getEvent(String payload, Map<String, String> headers, PaymentSecret configuration)
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
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata(INTERNAL_REFERENCE_METADATA_KEY, internalReference)
                                .build())
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
                    .status(PaymentInitiateStatus.PENDING)
                    .build();

        } catch (StripeException e) {
            throw new FailedPaymentInitiate(e.getMessage(), e);
        }

    }

    @Override
    public WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                       PaymentSecret configuration) throws InvalidWebhookPayload {
        log.info("Handling Stripe webhook for store {}", storeMerchantId);
        Event event = getEvent(payload, headers, configuration);

        log.info("Stripe webhook event type: {} version {}", event.getType(), event.getApiVersion());

        return switch (event.getType()) {
            case "checkout.session.completed" -> fromSession(event, PaymentUseCase.PAYMENT_SUCCEEDED);
            case "checkout.session.async_payment_succeeded" -> fromSession(event, PaymentUseCase.PAYMENT_SUCCEEDED);
            case "checkout.session.async_payment_failed" -> fromSession(event, PaymentUseCase.PAYMENT_FAILED);
            case "checkout.session.expired" -> fromSession(event, PaymentUseCase.PAYMENT_CANCELED);
            case "payment_intent.payment_failed" -> fromPaymentIntent(event, PaymentUseCase.PAYMENT_FAILED);
            default -> {
                log.info("Unhandled Stripe webhook event type: {}", event.getType());
                yield WebhookResult.noneUseCase();
            }
        };
    }

    private WebhookResult fromSession(Event event, PaymentUseCase paymentUseCase) {
        Session session = unSafeDeserialization(event, Session.class);
        return toWebhookResult(event, session.getClientReferenceId(), paymentUseCase);
    }

    private WebhookResult fromPaymentIntent(Event event, PaymentUseCase paymentUseCase) {
        PaymentIntent paymentIntent = unSafeDeserialization(event, PaymentIntent.class);
        String internalReference = paymentIntent.getMetadata() == null
                ? null
                : paymentIntent.getMetadata().get(INTERNAL_REFERENCE_METADATA_KEY);
        return toWebhookResult(event, internalReference, paymentUseCase);
    }

    private WebhookResult toWebhookResult(Event event, String internalReference, PaymentUseCase paymentUseCase) {
        if (internalReference == null || internalReference.isBlank()) {
            log.warn("Stripe event {} of type {} had no internal reference, ignoring", event.getId(),
                    event.getType());
            return WebhookResult.noneUseCase();
        }
        log.info("Processing Stripe event {} of type {} for internal reference {} as {}", event.getId(),
                event.getType(), internalReference, paymentUseCase);
        return WebhookResult.builder()
                .internalReference(internalReference)
                .paymentUseCase(paymentUseCase)
                .build();
    }

    @Override
    public PaymentType type() {
        return PaymentType.STRIPE;
    }


}

