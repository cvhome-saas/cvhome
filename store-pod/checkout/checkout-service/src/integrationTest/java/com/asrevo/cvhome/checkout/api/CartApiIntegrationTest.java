package com.asrevo.cvhome.checkout.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.checkout.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.checkout.errors.CheckoutErrors;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.CODE;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.SKU;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_A;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.STORE_B;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.V1;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.cartBody;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.expect;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.json;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.path;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.scoped;
import static com.asrevo.cvhome.checkout.api.CheckoutApiSupport.with;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The public cart over HTTP: the storefront's shape, live pricing, and the store as the only boundary.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class CartApiIntegrationTest {

    private static final String DISPLAYSUBTOTAL = "displaySubTotal";

    private static final String PRODUCT = "product";

    private static final String LIT_20_00 = "$20.00";

    private static final String SKU_B = "SKU-B";

    private static final String CART = "cart";

    private static final String SKU_2 = "sku";

    private static final String PRODUCTS = "products";

    private static final String QUANTITY = "quantity";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CheckoutApiSupport api;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
    }

    private String cartUrl(String store, String code) {
        return scoped(path(V1, CART, code), store);
    }

    @Test
    void aCartIsCreatedUpdatedReadAndEmptiedInTheStorefrontsShape() {
        ResponseEntity<String> created = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                cartBody(SKU, 2));
        expect(created, HttpStatus.CREATED);
        JsonNode cart = json(created);
        String code = cart.get(CODE).asString();
        assertThat(cart.get("id").asLong()).isPositive();
        assertThat(cart.get(QUANTITY).asInt()).isEqualTo(2);
        assertThat(cart.get("subtotal").asDouble()).isEqualTo(20.0);
        assertThat(cart.get(DISPLAYSUBTOTAL).asString()).isEqualTo(LIT_20_00);
        assertThat(cart.get("displayTotal").asString()).isEqualTo(LIT_20_00);
        assertThat(cart.get("totals")).hasSize(2);
        JsonNode line = cart.get(PRODUCTS).get(0);
        assertThat(line.get(SKU_2).asString()).isEqualTo(SKU);
        assertThat(line.get("description").get("name").asString()).isEqualTo(String.format("Product %s", SKU));
        assertThat(line.get("finalPrice").asString()).isEqualTo("$10.00");
        assertThat(line.get(DISPLAYSUBTOTAL).asString()).isEqualTo(LIT_20_00);
        assertThat(line.get("image").get("imageUrl").asString()).contains(SKU);
        assertThat(line.get("available").asBoolean()).isTrue();

        JsonNode updated = json(api.send(HttpMethod.PUT, cartUrl(STORE_A, code), null, cartBody(SKU_B, 1)));
        assertThat(updated.get(PRODUCTS)).hasSize(2);
        assertThat(updated.get(QUANTITY).asInt()).isEqualTo(3);

        JsonNode set = json(api.send(HttpMethod.PUT, cartUrl(STORE_A, code), null, cartBody(SKU, 5)));
        assertThat(set.get(QUANTITY).asInt()).isEqualTo(6);

        ResponseEntity<String> removed = api.send(HttpMethod.DELETE,
                scoped(path(V1, CART, code, PRODUCT, SKU_B), STORE_A), null, null);
        expect(removed, HttpStatus.NO_CONTENT);

        ResponseEntity<String> removedWithBody = api.send(HttpMethod.DELETE,
                with(scoped(path(V1, CART, code, PRODUCT, SKU), STORE_A), "body=true"), null, null);
        expect(removedWithBody, HttpStatus.OK);
        assertThat(json(removedWithBody).get(PRODUCTS)).isEmpty();

        JsonNode read = json(api.get(cartUrl(STORE_A, code), null));
        assertThat(read.get(CODE).asString()).isEqualTo(code);
        assertThat(read.get(QUANTITY).asInt()).isZero();
    }

    @Test
    void anotherStoreCannotSeeTheCart() {
        String code = api.newCart(STORE_A, SKU, 1);

        ResponseEntity<String> response = api.get(cartUrl(STORE_B, code), null);

        expect(response, HttpStatus.NOT_FOUND);
        assertThat(json(response).get(CODE).asString()).isEqualTo(CheckoutErrors.CART_NOT_FOUND.code());
    }

    @Test
    void unknownAndUnpurchasableSkusAreRefusedAsUnprocessable() {
        ResponseEntity<String> unknown = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                cartBody(ExternalClientsTestConfiguration.SKU_UNKNOWN, 1));
        expect(unknown, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(unknown).get(CODE).asString()).isEqualTo(CheckoutErrors.PRODUCT_NOT_PURCHASABLE.code());

        ResponseEntity<String> out = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                cartBody(ExternalClientsTestConfiguration.SKU_OUT, 1));
        expect(out, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(out).get("params").get(SKU_2).asString()).isEqualTo(ExternalClientsTestConfiguration.SKU_OUT);
    }

    @Test
    void aBlankSkuIsAValidationError() {
        ResponseEntity<String> response = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                "{\"product\":\"\",\"quantity\":1}");

        expect(response, HttpStatus.BAD_REQUEST);
    }

    @Test
    void aMissingStoreParameterIsRefused() {
        ResponseEntity<String> response = api.send(HttpMethod.POST, path(V1, CART), null, cartBody(SKU, 1));

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
