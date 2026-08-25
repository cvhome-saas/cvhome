package com.asrevo.cvhome.payment.service.processor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
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
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.stripe.exception.ApiConnectionException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Stripe is never called: session creation is intercepted at the static SDK entry point, and webhooks are signed here
 * with the same HMAC scheme Stripe uses, so the signature check runs for real against a known secret.
 */
class StripeProcessorTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String INTERNAL_REF = "tx-1";

    private static final String ORDER_REF = "order-1";

    private static final String SECRET_KEY = "sk_test_123";

    private static final String WEBHOOK_SECRET = "whsec_test_123";

    private static final String SESSION_ID = "cs_test_1";

    private static final String SESSION_URL = "https://checkout.stripe.com/c/pay/cs_test_1";

    private static final String SUCCESS_URL = "https://shop.example/ok";

    private static final String CANCEL_URL = "https://shop.example/cancel";

    private static final String SIGNATURE_HEADER = "stripe-signature";

    private static final String SIGNATURE_HEADER_TITLE_CASE = "Stripe-Signature";

    private static final String EVENT_ID = "evt_1";

    private static final String SESSION_COMPLETED = "checkout.session.completed";

    private static final String ASYNC_SUCCEEDED = "checkout.session.async_payment_succeeded";

    private static final String ASYNC_FAILED = "checkout.session.async_payment_failed";

    private static final String SESSION_EXPIRED = "checkout.session.expired";

    private static final String INTENT_FAILED = "payment_intent.payment_failed";

    private static final String SESSION_OBJECT = """
            {"id":"cs_1","object":"checkout.session","client_reference_id":"%s"}""";

    private static final String INTENT_OBJECT = """
            {"id":"pi_1","object":"payment_intent","metadata":{"internal_reference":"%s"}}""";

    private static final String INTENT_WITHOUT_METADATA = """
            {"id":"pi_1","object":"payment_intent"}""";

    private static final String BROKEN_SESSION_OBJECT = """
            {"id":"cs_1","object":"checkout.session","amount_total":"not-a-number"}""";

    private static final String EVENT = """
            {"id":"%s","object":"event","api_version":"2024-06-20","type":"%s","data":{"object":%s}}""";

    private static final int PAYMENT_REQUIRED = 402;

    private static final int UNAUTHORIZED = 401;

    private static final String CARD_DECLINED = "card_declined";

    private static final String INVALID_API_KEY = "invalid_api_key";

    private static final String SIGNATURE_PRESENT = "signaturePresent";

    private final StripeProcessor processor = new StripeProcessor();

    private final ReadablePaymentConfiguration secret = ReadablePaymentConfiguration.builder().storeMerchantId(STORE)
            .paymentType(PaymentType.STRIPE).secretKey(SECRET_KEY).webhookSecret(WEBHOOK_SECRET).enabled(true).build();

    private static PaymentRequest request() {
        return PaymentRequest.builder().ref(ORDER_REF).amount(new BigDecimal("19.99")).currency(new CurrencyCode("USD"))
                .paymentType(PaymentType.STRIPE).expireAt(Instant.ofEpochSecond(1_900_000_000L)).successUrl(SUCCESS_URL)
                .cancelUrl(CANCEL_URL).build();
    }

    private static String event(String type, String dataObject) {
        return String.format(EVENT, EVENT_ID, type, dataObject);
    }

    private static String sessionEvent(String type, String clientReferenceId) {
        return event(type, String.format(SESSION_OBJECT, clientReferenceId));
    }

    private static String sign(String payload, String secret) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        String signature = Webhook.Util.computeHmacSha256(secret, String.format("%d.%s", timestamp, payload));
        return String.format("t=%d,%s=%s", timestamp, Webhook.Signature.EXPECTED_SCHEME, signature);
    }

    private WebhookResult parse(String payload) throws Exception {
        return processor.parseWebhook(STORE, payload, Map.of(SIGNATURE_HEADER, sign(payload, WEBHOOK_SECRET)), secret);
    }

    @Test
    void typeIsStripe() {
        assertThat(processor.type()).isEqualTo(PaymentType.STRIPE);
    }

    @Test
    void initiateBuildsAHostedSessionKeyedByTheInternalReference() throws Exception {
        Session session = new Session();
        session.setId(SESSION_ID);
        session.setUrl(SESSION_URL);
        ArgumentCaptor<SessionCreateParams> params = ArgumentCaptor.forClass(SessionCreateParams.class);
        ArgumentCaptor<RequestOptions> options = ArgumentCaptor.forClass(RequestOptions.class);

        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenReturn(session);

            PaymentInitiateResult result = processor.initiate(INTERNAL_REF, secret, request());

            stripe.verify(() -> Session.create(params.capture(), options.capture()));
            assertThat(result.status()).isEqualTo(PaymentInitiateStatus.PENDING);
            assertThat(result.externalId()).isEqualTo(SESSION_ID);
            assertThat(result.redirectUrl()).isEqualTo(SESSION_URL);
        }

        assertThat(options.getValue().getApiKey()).isEqualTo(SECRET_KEY);
        SessionCreateParams sent = params.getValue();
        assertThat(sent.getMode()).isEqualTo(SessionCreateParams.Mode.PAYMENT);
        assertThat(sent.getClientReferenceId()).isEqualTo(INTERNAL_REF);
        assertThat(sent.getSuccessUrl()).isEqualTo(SUCCESS_URL);
        assertThat(sent.getCancelUrl()).isEqualTo(CANCEL_URL);
        assertThat(sent.getExpiresAt()).isEqualTo(1_900_000_000L);
        assertThat(sent.getPaymentIntentData().getMetadata()).containsEntry("internal_reference", INTERNAL_REF);
        SessionCreateParams.LineItem line = sent.getLineItems().getFirst();
        assertThat(line.getQuantity()).isEqualTo(1L);
        assertThat(line.getPriceData().getCurrency()).isEqualTo("usd");
        assertThat(line.getPriceData().getUnitAmount()).isEqualTo(1900L);
        assertThat(line.getPriceData().getProductData().getName()).contains(ORDER_REF);
    }

    @Test
    void aDeclinedCardIsARejectionCarryingStripesCode() {
        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenThrow(new CardException("declined", "req_1", CARD_DECLINED, "number", "insufficient_funds",
                            null, PAYMENT_REQUIRED, null));

            assertThatThrownBy(() -> processor.initiate(INTERNAL_REF, secret, request()))
                    .isInstanceOfSatisfying(PaymentInitiateRejectedException.class, e -> {
                        assertThat(e.providerCode()).isEqualTo(CARD_DECLINED);
                        assertThat(e.providerStatus()).isEqualTo(PAYMENT_REQUIRED);
                        assertThat(e.params()).containsEntry("internalReference", INTERNAL_REF);
                    });
        }
    }

    @Test
    void anyOtherStripeFailureIsAnUndecidedOutage() {
        try (MockedStatic<Session> stripe = mockStatic(Session.class)) {
            stripe.when(() -> Session.create(any(SessionCreateParams.class), any(RequestOptions.class)))
                    .thenThrow(new ApiConnectionException("no route"))
                    .thenThrow(new AuthenticationException("bad key", "req_2", INVALID_API_KEY, UNAUTHORIZED));

            assertThatThrownBy(() -> processor.initiate(INTERNAL_REF, secret, request()))
                    .isInstanceOfSatisfying(PaymentProviderUnavailableException.class,
                            e -> assertThat(e.providerStatus()).isZero());
            assertThatThrownBy(() -> processor.initiate(INTERNAL_REF, secret, request()))
                    .isInstanceOfSatisfying(PaymentProviderUnavailableException.class, e -> {
                        assertThat(e.providerStatus()).isEqualTo(UNAUTHORIZED);
                        assertThat(e.providerCode()).isEqualTo(INVALID_API_KEY);
                    });
        }
    }

    @Test
    void completedSessionSucceedsThePaymentUnderItsClientReference() throws Exception {
        WebhookResult result = parse(sessionEvent(SESSION_COMPLETED, INTERNAL_REF));

        assertThat(result.paymentUseCase()).isEqualTo(PaymentUseCase.PAYMENT_SUCCEEDED);
        assertThat(result.internalReference()).isEqualTo(INTERNAL_REF);
    }

    @Test
    void everySessionEventTypeMapsToItsUseCase() throws Exception {
        assertThat(parse(sessionEvent(ASYNC_SUCCEEDED, INTERNAL_REF)).paymentUseCase())
                .isEqualTo(PaymentUseCase.PAYMENT_SUCCEEDED);
        assertThat(parse(sessionEvent(ASYNC_FAILED, INTERNAL_REF)).paymentUseCase())
                .isEqualTo(PaymentUseCase.PAYMENT_FAILED);
        assertThat(parse(sessionEvent(SESSION_EXPIRED, INTERNAL_REF)).paymentUseCase())
                .isEqualTo(PaymentUseCase.PAYMENT_CANCELED);
    }

    @Test
    void failedPaymentIntentIsKeyedByItsMetadata() throws Exception {
        WebhookResult result = parse(event(INTENT_FAILED, String.format(INTENT_OBJECT, INTERNAL_REF)));

        assertThat(result.paymentUseCase()).isEqualTo(PaymentUseCase.PAYMENT_FAILED);
        assertThat(result.internalReference()).isEqualTo(INTERNAL_REF);
    }

    @Test
    void eventsWithoutAReferenceAndUnknownTypesAreNoOps() throws Exception {
        assertThat(parse(event(INTENT_FAILED, INTENT_WITHOUT_METADATA)).paymentUseCase())
                .isEqualTo(PaymentUseCase.NONE);
        assertThat(parse(sessionEvent(SESSION_COMPLETED, "")).paymentUseCase()).isEqualTo(PaymentUseCase.NONE);
        assertThat(parse(event("charge.refunded", INTENT_WITHOUT_METADATA)).paymentUseCase())
                .isEqualTo(PaymentUseCase.NONE);
    }

    @Test
    void titleCaseSignatureHeaderIsAccepted() throws Exception {
        String payload = sessionEvent(SESSION_COMPLETED, INTERNAL_REF);

        WebhookResult result = processor.parseWebhook(STORE, payload,
                Map.of(SIGNATURE_HEADER_TITLE_CASE, sign(payload, WEBHOOK_SECRET)), secret);

        assertThat(result.internalReference()).isEqualTo(INTERNAL_REF);
    }

    @Test
    void wrongSecretAndMissingSignatureAreBothRefusedButToldApart() throws Exception {
        String payload = sessionEvent(SESSION_COMPLETED, INTERNAL_REF);
        Map<String, String> wrongKey = Map.of(SIGNATURE_HEADER, sign(payload, "whsec_other"));

        assertThatThrownBy(() -> processor.parseWebhook(STORE, payload, wrongKey, secret))
                .isInstanceOfSatisfying(InvalidWebhookSignatureException.class,
                        e -> assertThat(e.params()).containsEntry(SIGNATURE_PRESENT, true));
        assertThatThrownBy(() -> processor.parseWebhook(STORE, payload, Map.of(), secret))
                .isInstanceOfSatisfying(InvalidWebhookSignatureException.class,
                        e -> assertThat(e.params()).containsEntry(SIGNATURE_PRESENT, false));
    }

    @Test
    void aSessionEventCarryingAnotherObjectIsAMappingFault() {
        String payload = event(SESSION_COMPLETED, String.format(INTENT_OBJECT, INTERNAL_REF));

        assertThatThrownBy(() -> parse(payload)).isInstanceOfSatisfying(UnexpectedWebhookObjectException.class,
                e -> assertThat(e.params()).containsEntry("expectedType", "Session").containsEntry("eventId", EVENT_ID));
    }

    @Test
    void anUndeserializableObjectIsAnUnreadablePayload() {
        String payload = event(SESSION_COMPLETED, BROKEN_SESSION_OBJECT);

        assertThatThrownBy(() -> parse(payload)).isInstanceOfSatisfying(UnreadableWebhookPayloadException.class,
                e -> assertThat(e.params()).containsEntry("eventType", SESSION_COMPLETED));
    }

}
