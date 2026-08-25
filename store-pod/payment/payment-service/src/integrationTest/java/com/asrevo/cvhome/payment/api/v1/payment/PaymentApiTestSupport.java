package com.asrevo.cvhome.payment.api.v1.payment;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

/**
 * The payment API's URLs, bodies and tokens over the shared {@link ApiClient} / {@link Tokens}.
 *
 * <p>
 * {@link #stripeSignature} reimplements Stripe's signing scheme in six lines rather than pulling {@code stripe-java}
 * onto the service's test classpath: the service module never sees the SDK, and a webhook test that signs its own
 * payload is the only way to exercise the real signature check without a network call to Stripe.
 * </p>
 */
final class PaymentApiTestSupport {

    static final String PRIVATE_CONFIG = "/api/v1/private/payment-configuration";

    static final String PUBLIC_CONFIG = "/api/v1/public/payment-configuration";

    static final String INITIATE = "/api/v1/private/payments/initiate";

    static final String PAYMENTS = "/api/v1/private/payments";

    static final String TRANSACTIONS = "/api/v1/private/payment/transactions";

    static final String TRANSACTION = "/api/v1/private/payment/transaction";

    static final String WEBHOOK = "/api/v1/public/webhook";

    static final String SUPPORTED_TYPES = "supported-payment-types";

    static final String STRIPE = "STRIPE";

    static final String COD = "COD";

    static final String PAYPAL = "PAYPAL";

    static final String MANUAL_TRANSFER = "MANUAL_TRANSFER";

    static final String PAYMENT_TYPE = "paymentType";

    static final String STATUS = "status";

    /** The path segment of the payment-status endpoint, which is the same word. */
    static final String STATUS_SEGMENT = STATUS;

    /** Prefix for the throwaway order references every staged transaction is keyed by. */
    static final String ORDER = "order";

    static final String CONTENT = "content";

    static final String CODE = "code";

    static final String GATEWAY_REF = "gatewayRef";

    static final String REQUEST_REF = "requestRef";

    static final String INTERNAL_REF = "internalRef";

    static final String ENABLED = "enabled";

    static final String API_KEY = "apiKey";

    static final String WEBHOOK_SECRET = "webhookSecret";

    static final String PENDING = "PENDING";

    static final String PAID = "PAID";

    static final String FAILED = "FAILED";

    static final String SIGNATURE_HEADER = "Stripe-Signature";

    static final String HMAC_SHA_256 = "HmacSHA256";

    /** A {@code PersistablePaymentConfiguration}: type, apiKey, secretKey, webhookSecret, enabled. */
    static final String CONFIG_BODY = """
            {"paymentType":"%s","apiKey":"%s","secretKey":"%s","webhookSecret":"%s","enabled":%b}""";

    /** A {@code PaymentRequest}: ref, amount, currency, type, expiry, success and cancel URLs. */
    static final String PAYMENT_REQUEST_BODY = """
            {"ref":"%s","amount":%s,"currency":{"code":"USD"},"paymentType":"%s","expireAt":"%s",
             "successUrl":"https://shop.example/ok","cancelUrl":"https://shop.example/cancel"}""";

    /** A Stripe {@code checkout.session.completed} event keyed by {@code client_reference_id}. */
    static final String SESSION_COMPLETED_EVENT = """
            {"id":"evt_it_1","object":"event","api_version":"2024-06-20","type":"checkout.session.completed",
             "data":{"object":{"id":"cs_it_1","object":"checkout.session","client_reference_id":"%s"}}}""";

    private final ApiClient client;

    private final RestClient raw;

    private final Tokens tokens;

    PaymentApiTestSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.raw = RestClient.builder().baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(s -> true, (request, response) -> { })
                .build();
        this.tokens = new Tokens(signer);
    }

    static String scoped(String path, String store) {
        return ApiClient.scoped(path, store);
    }

    static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    static String query(String path, String query) {
        return ApiClient.query(path, query);
    }

    static JsonNode json(ResponseEntity<String> response) {
        return ApiClient.json(response);
    }

    static String slug(String prefix) {
        return ApiClient.slug(prefix);
    }

    static void expect(ResponseEntity<String> response, HttpStatus status) {
        ApiClient.expect(response, status);
    }

    static String configBody(String type, String apiKey, String secretKey, String webhookSecret, boolean enabled) {
        return String.format(CONFIG_BODY, type, apiKey, secretKey, webhookSecret, enabled);
    }

    static String paymentRequestBody(String ref, String amount, String type) {
        return String.format(PAYMENT_REQUEST_BODY, ref, amount, type, Instant.now().plusSeconds(3600));
    }

    /**
     * {@code t=<epoch>,v1=<hmac-sha256 hex of "<epoch>.<payload>">} — Stripe's scheme, exactly.
     */
    static String stripeSignature(String payload, String secret) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance(HMAC_SHA_256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
        byte[] digest = mac.doFinal(String.format("%d.%s", timestamp, payload).getBytes(StandardCharsets.UTF_8));
        return String.format("t=%d,v1=%s", timestamp, HexFormat.of().formatHex(digest));
    }

    String admin(String store) {
        return tokens.staff(Tokens.ROLE_STORE_ADMIN, store);
    }

    String moderator(String store) {
        return tokens.staff(Tokens.ROLE_STORE_MODERATOR, store);
    }

    String s2s() {
        return tokens.s2s(Tokens.SCOPE_STORE_POD, "payment");
    }

    ResponseEntity<String> get(String url, String token) {
        return client.get(url, token);
    }

    ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        return client.send(method, url, token, body);
    }

    ResponseEntity<String> post(String url, String token, String body) {
        return client.send(HttpMethod.POST, url, token, body);
    }

    /**
     * A webhook delivery: no bearer token, the provider's signature header instead.
     */
    ResponseEntity<String> postSigned(String url, String body, String signature) {
        return raw.post().uri(url).contentType(MediaType.APPLICATION_JSON).header(SIGNATURE_HEADER, signature)
                .body(body).retrieve().toEntity(String.class);
    }

    /**
     * A webhook delivery carrying no signature header at all.
     */
    ResponseEntity<String> postUnsigned(String url, String body) {
        return raw.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(body).retrieve()
                .toEntity(String.class);
    }

}
