package com.asrevo.cvhome.checkout.api;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

/**
 * The checkout API's URL and principal conventions over the shared {@link ApiClient} / {@link Tokens}: a shopper of
 * store A, a seller of store A, the pod's own service principal, and their store-B counterparts for the isolation
 * cases. Carts are created through the API in each test — nothing is seeded, so every case starts from nothing.
 */
public final class CheckoutApiSupport {

    public static final String STORE_A = Tokens.STORE_1;

    /** Requires a signed-in shopper for placement (see {@code ExternalClientsTestConfiguration}). */
    public static final String STORE_B = Tokens.STORE_2;

    public static final String POD = "pod-507f1f77";

    /** The property that names this pod, so the s2s token's {@code resource} claim matches. */
    public static final String POD_PROPERTY = "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77";

    public static final String SHOPPER_A = "shopper-a";

    public static final String SHOPPER_A2 = "shopper-a2";

    public static final String SHOPPER_B = "shopper-b";

    public static final String V1 = "/api/v1";

    public static final String V1_PRIVATE = "/api/v1/private";

    public static final String SKU = "SKU-NK-RUN-001";

    public static final String CART_BODY = "{\"product\":\"%s\",\"quantity\":%d}";

    public static final String CHECKOUT_BODY = """
            {"paymentType":"%s","customer":{"emailAddress":"%s","billing":{"firstName":"Ada","lastName":"Lovelace",
            "address":"1 Analytical Way","city":"London","postalCode":"N1","phone":"0044 1","country":"GB"}}}
            """;

    public static final String CODE = "code";

    public static final String ID = "id";

    public static final String ORDER_STATUS = "orderStatus";

    public static final String PAYMENT_STATUS = "paymentStatus";

    public static final String REDIRECT_URL = "redirectUrl";

    private static final String CART = "cart";

    private final ApiClient client;

    private final Tokens tokens;

    public CheckoutApiSupport(int port, TestJwtSigner signer) {
        this.client = new ApiClient(port);
        this.tokens = new Tokens(signer);
    }

    public static String scoped(String path, String store) {
        return ApiClient.scoped(path, store);
    }

    public static String path(Object... segments) {
        return ApiClient.path(segments);
    }

    /** {@code url} with {@code query} appended after the right separator. */
    public static String with(String url, String query) {
        return ApiClient.query(url, query);
    }

    public static JsonNode json(ResponseEntity<String> response) {
        return ApiClient.json(response);
    }

    public static void expect(ResponseEntity<String> response, HttpStatus status) {
        ApiClient.expect(response, status);
    }

    public static String cartBody(String sku, int quantity) {
        return String.format(CART_BODY, sku, quantity);
    }

    public static String checkoutBody(String paymentType, String email) {
        return String.format(CHECKOUT_BODY, paymentType, email);
    }

    public String shopper(String store, String sub) {
        return tokens.shopper(store, sub);
    }

    public String admin(String store) {
        return tokens.staff(Tokens.ROLE_STORE_ADMIN, store);
    }

    public String moderator(String store) {
        return tokens.staff(Tokens.ROLE_STORE_MODERATOR, store);
    }

    public String s2s() {
        return tokens.s2s(Tokens.SCOPE_STORE_POD, POD);
    }

    public ResponseEntity<String> get(String url, String token) {
        return client.get(url, token);
    }

    public ResponseEntity<String> send(HttpMethod method, String url, String token, String body) {
        return client.send(method, url, token, body);
    }

    /** A fresh cart in {@code store} holding {@code quantity} of {@code sku}; answers its code. */
    public String newCart(String store, String sku, int quantity) {
        ResponseEntity<String> response = send(HttpMethod.POST, scoped(path(V1, CART), store), null,
                cartBody(sku, quantity));
        expect(response, HttpStatus.CREATED);
        return json(response).get(CODE).asString();
    }

    /** Places {@code cart} as {@code token} (null for a guest) and answers the raw response. */
    public ResponseEntity<String> checkout(String store, String cart, String token, String paymentType, String email) {
        return send(HttpMethod.POST, scoped(path(V1, CART, cart, "checkout"), store), token,
                checkoutBody(paymentType, email));
    }

    public JsonNode placed(String store, String cart, String token, String paymentType, String email) {
        ResponseEntity<String> response = checkout(store, cart, token, paymentType, email);
        expect(response, HttpStatus.CREATED);
        return json(response);
    }
}
