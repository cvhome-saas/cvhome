package com.asrevo.cvhome.checkout.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

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

    private static final String DESCRIPTION_FIELD = "description";

    private static final String NAME_FIELD = "name";

    private static final String PRODUCT_PREFIX = "Product ";

    private static final String DISPLAYSUBTOTAL = "displaySubTotal";

    private static final String PRODUCT = "product";

    private static final String LIT_20_00 = "$20.00";

    private static final String LIT_10_00 = "$10.00";

    private static final String FINAL_PRICE = "finalPrice";

    private static final String SKU_B = "SKU-B";

    private static final String CART = "cart";

    private static final String SKU_2 = "sku";

    private static final String PRODUCTS = "products";

    private static final String QUANTITY = "quantity";

    /** A dot was never part of a sku, so no catalog variant can carry this one. */
    private static final String MALFORMED = "SKU.DOT";

    private static final String ALREADY_CONVERTED = "CHECKOUT.CART.ALREADY_CONVERTED";

    private static final String ID = "id";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private JdbcTemplate jdbc;

    private CheckoutApiSupport api;

    @BeforeEach
    void setUp() {
        api = new CheckoutApiSupport(port, signer);
    }

    private String cartUrl(String store, String code) {
        return scoped(path(V1, CART, code), store);
    }

    @Test
    void aLineTheCatalogNoLongerKnowsIsLeftOutOfTheCartAndPrunedFromIt() {
        JsonNode created = json(api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null, cartBody(SKU, 1)));
        long cartId = created.get(ID).asLong();
        // A sku the catalog has dropped since it was added: the API refuses one now, so only a direct row makes it.
        jdbc.update("insert into checkout.cart_line (line_id, cart_id, sku, quantity) values (?, ?, ?, 1)",
                990_000L + cartId, cartId, ExternalClientsTestConfiguration.SKU_UNKNOWN);

        JsonNode read = json(api.get(cartUrl(STORE_A, created.get(CODE).asString()), null));

        assertThat(read.get(PRODUCTS)).hasSize(1);
        assertThat(jdbc.queryForObject("select count(*) from checkout.cart_line where cart_id = ?", Integer.class,
                cartId)).as("the read pruned the line, so placement will not refuse it").isEqualTo(1);
    }

    @Test
    void aCartThatBecameAnOrderCannotBeChanged() {
        String code = api.newCart(STORE_A, SKU, 1);
        api.placed(STORE_A, code, null, "COD", "converted@example.com");

        ResponseEntity<String> edit = api.send(HttpMethod.PUT, cartUrl(STORE_A, code), null, cartBody(SKU, 1));

        expect(edit, HttpStatus.CONFLICT);
        assertThat(json(edit).get(CODE).asString()).isEqualTo(ALREADY_CONVERTED);
    }

    @Test
    void aCartIsCreatedUpdatedReadAndEmptiedInTheStorefrontsShape() {
        ExternalClientsTestConfiguration.PRICED_INSIDE_A_TRANSACTION.set(false);
        ResponseEntity<String> created = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                cartBody(SKU, 2));
        expect(created, HttpStatus.CREATED);
        JsonNode cart = json(created);
        String code = cart.get(CODE).asString();
        assertThat(cart.get(ID).asLong()).isPositive();
        assertThat(cart.get(QUANTITY).asInt()).isEqualTo(2);
        assertThat(cart.get("subtotal").asDouble()).isEqualTo(20.0);
        assertThat(cart.get(DISPLAYSUBTOTAL).asString()).isEqualTo(LIT_20_00);
        assertThat(cart.get("displayTotal").asString()).isEqualTo(LIT_20_00);
        assertThat(cart.get("totals")).hasSize(2);
        JsonNode line = cart.get(PRODUCTS).get(0);
        assertThat(line.get(SKU_2).asString()).isEqualTo(SKU);
        assertThat(line.get(DESCRIPTION_FIELD).get(NAME_FIELD).asString()).isEqualTo(String.format("%s%s", PRODUCT_PREFIX, SKU));
        assertThat(line.get(FINAL_PRICE).asString()).isEqualTo(LIT_10_00);
        assertThat(line.get(DISPLAYSUBTOTAL).asString()).isEqualTo(LIT_20_00);
        assertThat(line.get("image").get("imageUrl").asString()).contains(SKU);
        assertThat(line.get("available").asBoolean()).isTrue();

        JsonNode updated = json(api.send(HttpMethod.PUT, cartUrl(STORE_A, code), null, cartBody(SKU_B, 1)));
        assertThat(updated.get(PRODUCTS)).hasSize(2);
        assertThat(updated.get(QUANTITY).asInt()).isEqualTo(3);
        int catalogReadsAfterAdds = ExternalClientsTestConfiguration.CART_LINE_READS.get();
        JsonNode reread = json(api.get(cartUrl(STORE_A, code), null));
        assertThat(reread.get(PRODUCTS)).hasSize(2);
        assertThat(reread.get(PRODUCTS).get(0).get(DESCRIPTION_FIELD).get(NAME_FIELD).asString()).startsWith(PRODUCT_PREFIX);
        assertThat(ExternalClientsTestConfiguration.CART_LINE_READS.get())
                .as("two lines remembered from their adds: a read asks the catalogue nothing").isEqualTo(catalogReadsAfterAdds);
        int inventoryReadsAfterFirstRead = ExternalClientsTestConfiguration.INVENTORY_READS.get();
        JsonNode rereadAgain = json(api.get(cartUrl(STORE_A, code), null));
        assertThat(rereadAgain.get(PRODUCTS)).hasSize(2);
        assertThat(rereadAgain.get(PRODUCTS).get(0).get(FINAL_PRICE).asString()).isEqualTo(LIT_10_00);
        assertThat(ExternalClientsTestConfiguration.INVENTORY_READS.get())
                .as("the second read of the same cart within seconds prices from the per-sku cache")
                .isEqualTo(inventoryReadsAfterFirstRead);

        JsonNode set = json(api.send(HttpMethod.PUT, cartUrl(STORE_A, code), null, cartBody(SKU, 5)));
        assertThat(set.get(QUANTITY).asInt()).isEqualTo(6);

        ResponseEntity<String> removed = api.send(HttpMethod.DELETE,
                scoped(path(V1, CART, code, PRODUCT, SKU_B), STORE_A), null, null);
        expect(removed, HttpStatus.NO_CONTENT);

        ResponseEntity<String> removedWithBody = api.send(HttpMethod.DELETE,
                with(scoped(path(V1, CART, code, PRODUCT, SKU), STORE_A), "body=true"), null, null);
        expect(removedWithBody, HttpStatus.OK);
        assertThat(json(removedWithBody).get(PRODUCTS)).isEmpty();

        int catalogReadsBefore = ExternalClientsTestConfiguration.CART_LINE_READS.get();
        JsonNode read = json(api.get(cartUrl(STORE_A, code), null));
        assertThat(read.get(CODE).asString()).isEqualTo(code);
        assertThat(read.get(QUANTITY).asInt()).isZero();
        assertThat(ExternalClientsTestConfiguration.CART_LINE_READS.get())
                .as("a cart read prices its lines from what they remember; the catalogue is not asked")
                .isEqualTo(catalogReadsBefore);
        assertThat(ExternalClientsTestConfiguration.PRICED_INSIDE_A_TRANSACTION.get())
                .as("catalog, inventory or merchant was called while a database transaction was open").isFalse();
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

    /**
     * A line whose sku could never exist is a 400 naming the field, decided at the edge, rather than a "not
     * purchasable" 422 learned from asking catalog and inventory about it. The path form is refused the same way, and
     * the cart is untouched.
     */
    @Test
    void aMalformedSkuIsRefusedAtTheEdgeInTheBodyAndInThePath() {
        ResponseEntity<String> added = api.send(HttpMethod.POST, scoped(path(V1, CART), STORE_A), null,
                cartBody(MALFORMED, 1));
        expect(added, HttpStatus.BAD_REQUEST);
        assertThat(json(added).get(CODE).asString()).isEqualTo("COMMON.VALIDATION_FAILED");
        assertThat(json(added).path("fieldErrors").findValuesAsString("field")).containsExactly(PRODUCT);

        String code = api.newCart(STORE_A, SKU, 1);
        ResponseEntity<String> removed = api.send(HttpMethod.DELETE,
                scoped(path(V1, CART, code, PRODUCT, MALFORMED), STORE_A), null, null);
        expect(removed, HttpStatus.BAD_REQUEST);
        assertThat(json(removed).get(CODE).asString()).isEqualTo("COMMON.MALFORMED_REQUEST");
        assertThat(json(api.get(cartUrl(STORE_A, code), null)).get(QUANTITY).asInt()).isEqualTo(1);
    }

    @Test
    void aMissingStoreParameterIsRefused() {
        ResponseEntity<String> response = api.send(HttpMethod.POST, path(V1, CART), null, cartBody(SKU, 1));

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
