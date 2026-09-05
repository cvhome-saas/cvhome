package com.asrevo.cvhome.payment.service.processor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentSecret;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentInitiateRejectedException;
import com.asrevo.cvhome.payment.errors.PaymentProviderUnavailableException;
import com.asrevo.cvhome.payment.errors.UnexpectedWebhookObjectException;
import com.asrevo.cvhome.payment.errors.UnreadableWebhookPayloadException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.model.payment.PaymentUseCase;
import com.asrevo.cvhome.payment.model.payment.WebhookResult;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.stripe.exception.CardException;
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

    /**
     * Stripe takes the amount in the currency's smallest unit. Two digits everywhere the platform sells
     * today; a zero-decimal currency (JPY) would need this to become per-currency rather than a constant.
     */
    private static final int MINOR_UNIT_DIGITS = 2;

    private static final String INTERNAL_REFERENCE_METADATA_KEY = "internal_reference";

    private static final String STRIPE = "stripe";

    private static final String SIGNATURE_HEADER = "stripe-signature";

    private static final String SIGNATURE_HEADER_TITLE_CASE = "Stripe-Signature";

    private static <T> T unSafeDeserialization(Event event, Class<T> clazz)
            throws UnexpectedWebhookObjectException, UnreadableWebhookPayloadException {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().deserializeUnsafe();
            return clazz.cast(stripeObject);
        } catch (ClassCastException e) {
            throw UnexpectedWebhookObjectException.of(STRIPE, event.getId(), event.getType(), clazz, e);
        } catch (EventDataObjectDeserializationException e) {
            throw UnreadableWebhookPayloadException.of(STRIPE, event.getId(), event.getType(), e);
        }
    }

    private static Event getEvent(String payload, Map<String, String> headers, PaymentSecret configuration)
            throws InvalidWebhookSignatureException {
        String sigHeader = headers.get(SIGNATURE_HEADER);
        if (sigHeader == null) {
            sigHeader = headers.get(SIGNATURE_HEADER_TITLE_CASE);
        }
        if (sigHeader == null) {
            // Stripe's verifier dereferences the header before it validates it, so an unsigned request would leave
            // here as a NullPointerException — a 500 for what is the most ordinary probe a public endpoint sees.
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, false, null);
        }
        try {
            return Webhook.constructEvent(payload, sigHeader, configuration.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            // Deliberately not logged here: the caller logs it once with the store context, and a failed signature is
            // an expected condition on a public endpoint, not an incident worth a stack trace at every layer.
            throw InvalidWebhookSignatureException.verificationFailed(STRIPE, true, e);
        }
    }

    /**
     * The amount in the currency's smallest unit, which is what Stripe charges.
     *
     * <p>
     * {@code longValue()} <em>truncates</em>: written as {@code amount.longValue() * 100}, an order for 12.34 was
     * sent as 1200 and the shopper was charged 12.00. Every fractional amount lost its minor units silently, and
     * nothing downstream could notice, because Stripe was told a whole number that was internally consistent.
     * </p>
     *
     * <p>
     * Scaling the {@link BigDecimal} keeps the minor units; {@code longValueExact} then refuses rather than
     * truncating if an amount ever arrives with more precision than the currency has, which is a loud failure
     * instead of another quiet under-charge.
     * </p>
     */
    private static long toStripeUnitAmount(BigDecimal amount) {
        return amount.movePointRight(MINOR_UNIT_DIGITS).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    /**
     * Stripe leaves the status null when the call never reached it at all, which is exactly the {@code 0} the provider
     * metadata uses for "no response".
     */
    private static int statusOf(StripeException e) {
        return e.getStatusCode() == null ? 0 : e.getStatusCode();
    }

    @Override
    public PaymentInitiateResult initiate(String internalReference, PaymentSecret secret, PaymentRequest request)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException {
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
                                                .setUnitAmount(toStripeUnitAmount(request.amount()))
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(String.format("Order #%s", request.ref()))
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

        } catch (CardException e) {
            // The one Stripe failure that is an answer rather than a fault: the card was refused, the shopper must use
            // another one, and retrying this request unchanged will be refused again.
            throw PaymentInitiateRejectedException.of(STRIPE, request.ref(), internalReference, e.getCode(),
                    statusOf(e), e);
        } catch (StripeException e) {
            // Everything else — no connection, rate limited, our API key rejected, a malformed request we built —
            // settles nothing about the payment. Calling any of those a rejection would tell a caller to cancel an
            // order that may well have been charged.
            throw PaymentProviderUnavailableException.of(STRIPE, request.ref(), internalReference, e.getCode(),
                    statusOf(e), e);
        }

    }

    @Override
    public WebhookResult parseWebhook(StoreMerchantId storeMerchantId, String payload, Map<String, String> headers,
                                      PaymentSecret configuration)
            throws InvalidWebhookSignatureException, UnreadableWebhookPayloadException,
            UnexpectedWebhookObjectException {
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

    private WebhookResult fromSession(Event event, PaymentUseCase paymentUseCase)
            throws UnexpectedWebhookObjectException, UnreadableWebhookPayloadException {
        Session session = unSafeDeserialization(event, Session.class);
        return toWebhookResult(event, session.getClientReferenceId(), paymentUseCase);
    }

    private WebhookResult fromPaymentIntent(Event event, PaymentUseCase paymentUseCase)
            throws UnexpectedWebhookObjectException, UnreadableWebhookPayloadException {
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

