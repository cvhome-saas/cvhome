package com.asrevo.cvhome.payment.service.processor;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.payment.service.processor.exception.FailedPaymentInitiate;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidPaymentReferenceId;
import com.asrevo.cvhome.payment.service.processor.exception.InvalidWebhookPayload;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
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

    private static final String EVENT_CHECKOUT_COMPLETED = "checkout.session.completed";
    private static final String EVENT_CHECKOUT_EXPIRED = "checkout.session.expired";
    private static final String EVENT_PAYMENT_INTENT_FAILED = "payment_intent.payment_failed";

    private static PaymentUseCase resolveUseCase(String eventType) {
        return switch (eventType) {
            case EVENT_CHECKOUT_COMPLETED -> PaymentUseCase.PAYMENT_SUCCEEDED;
            case EVENT_CHECKOUT_EXPIRED, EVENT_PAYMENT_INTENT_FAILED -> PaymentUseCase.PAYMENT_FAILED;
            default -> PaymentUseCase.UNKNOWN;
        };
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
                    .transactionId(transactionId)
                    .status(PaymentInitiateStatus.PENDING)
                    .redirectUrl(session.getUrl())
                    .externalId(session.getId())
                    .build();

        } catch (StripeException e) {
            throw new FailedPaymentInitiate(e.getMessage(), e);
        }

    }

    @Override
    public WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                      PaymentConfiguration configuration) throws InvalidWebhookPayload {
        log.info("Handling Stripe webhook for store {}", storeMerchantId);
        Event event = getEvent(payload, headers, configuration);

        log.info("Stripe webhook event type: {} version {}", event.getType(), event.getApiVersion());

        PaymentUseCase useCase = resolveUseCase(event.getType());
        if (useCase == PaymentUseCase.UNKNOWN) {
            log.info("Ignoring unhandled Stripe event type: {}", event.getType());
            return WebhookResult.builder().paymentUseCase(PaymentUseCase.UNKNOWN).build();
        }

        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        Long transactionId = getTransactionId(session);
        PaymentStatus status = useCase == PaymentUseCase.PAYMENT_SUCCEEDED ? PaymentStatus.PAID : PaymentStatus.FAILED;
        log.info("Processing Stripe session for transaction ID: {}, useCase: {}", transactionId, useCase);

        return WebhookResult.builder()
                .transactionId(transactionId)
                .status(status)
                .paymentUseCase(useCase)
                .build();
    }


}

