package com.asrevo.cvhome.catalog.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CODE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CONTENT;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTION;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTIONS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.EXISTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.NAME;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.PRODUCTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.SKU;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.query;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The merchandising strips and a product's related items — one API and one entity, addressed two ways: a
 * store-level group has a code and no parent product, a product's related items are the {@code RELATED_ITEM} group
 * whose parent is that product.
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductGroupApiIntegrationTest {

    private static final String GROUPS_SEGMENT = "groups";

    private static final String GRP = "grp";

    private static final String PUBLIC_GROUPS = path(V1, PRODUCTS, GROUPS_SEGMENT);

    private static final String PRIVATE_GROUPS = path(V1_PRIVATE, PRODUCTS, GROUPS_SEGMENT);

    private static final String UNIQUE = path(PRIVATE_GROUPS, "unique");

    private static final String PRODUCT_SEGMENT = "product";

    private static final String RELATIONSHIP = "relationship";

    private static final String ACTIVE = "active";

    private static final String PARENT_PRODUCT = "parentProduct";

    /** Seeded store-level strip of store A. */
    private static final String FEATURED_ITEMS = "FEATURED_ITEMS";

    /** Seeded products of store A. */
    private static final long PRODUCT_ONE = 1L;

    private static final long PRODUCT_TWO = 2L;

    private static final long PRODUCT_THREE = 3L;

    private static final String GROUP_BODY = """
            {"code":"%s","active":%s,"productIds":[%s],
             "descriptions":[{"language":"en","name":"%s","title":"T","description":"d","friendlyUrl":"%s"},
                             {"language":"ar","name":"مجموعة"}]}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CatalogApiSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new CatalogApiSupport(port, signer);
        admin = api.token(ADMIN, STORE_A);
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private static String body(String code, boolean active, String productIds, String name) {
        return String.format(GROUP_BODY, code, active, productIds, name, code.toLowerCase());
    }

    private long save(String code, boolean active, String productIds) {
        var saved = api.send(HttpMethod.POST, scoped(PRIVATE_GROUPS, STORE_A), admin,
                body(code, active, productIds, code));
        expect(saved, HttpStatus.CREATED);
        return json(saved).get(ID).asLong();
    }

    private JsonNode read(String code) {
        var response = api.get(scoped(path(PRIVATE_GROUPS, code), STORE_A), admin);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    // ------------------------------------------------------------------------------------------------- strips

    @Test
    void groupLifecycleSaveReadAmendDelete() {
        String code = slug(GRP).toUpperCase();
        long id = save(code, true, String.valueOf(PRODUCT_ONE));

        JsonNode created = read(code);
        assertThat(created.get(ID).asLong()).isEqualTo(id);
        assertThat(created.get(ACTIVE).asBoolean()).isTrue();
        assertThat(created.get(PRODUCTS)).hasSize(1);
        assertThat(created.get(PRODUCTS).get(0).get(SKU).asString()).isNotEmpty();
        // the private read answers every language; the storefront read answers one
        assertThat(created.get(DESCRIPTIONS)).hasSize(2);
        assertThat(json(api.get(scoped(path(PUBLIC_GROUPS, code), STORE_A), null)).get(DESCRIPTIONS).isEmpty())
                .isTrue();

        // the save is an upsert on the code: saving again replaces the members rather than creating a second group
        long again = save(code, false, String.format("%d,%d", PRODUCT_ONE, PRODUCT_TWO));
        assertThat(again).isEqualTo(id);
        assertThat(read(code).get(PRODUCTS)).hasSize(2);
        assertThat(read(code).get(ACTIVE).asBoolean()).isFalse();

        // members can be added and removed one at a time
        expect(api.send(HttpMethod.POST, scoped(path(PRIVATE_GROUPS, code, PRODUCT_SEGMENT, PRODUCT_THREE), STORE_A),
                admin, null), HttpStatus.CREATED);
        assertThat(read(code).get(PRODUCTS)).hasSize(3);
        // adding one already in the group is a no-op, not a duplicate row
        expect(api.send(HttpMethod.POST, scoped(path(PRIVATE_GROUPS, code, PRODUCT_SEGMENT, PRODUCT_THREE), STORE_A),
                admin, null), HttpStatus.CREATED);
        assertThat(read(code).get(PRODUCTS)).hasSize(3);
        expect(api.send(HttpMethod.DELETE,
                scoped(path(PRIVATE_GROUPS, code, PRODUCT_SEGMENT, PRODUCT_THREE), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        assertThat(read(code).get(PRODUCTS)).hasSize(2);

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_GROUPS, code), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        expect(api.get(scoped(path(PRIVATE_GROUPS, code), STORE_A), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void theSeededStripsAreListedAndAnsweredByCode() {
        JsonNode list = json(api.get(scoped(PRIVATE_GROUPS, STORE_A), admin));
        assertThat(list.get(CONTENT)).isNotEmpty();
        // the list is a summary: it names the groups without dragging every member along
        assertThat(list.get(CONTENT).valueStream().anyMatch(g -> FEATURED_ITEMS.equals(g.get(CODE).asString()))).isTrue();
        assertThat(list.get(CONTENT).valueStream().allMatch(g -> g.get(PRODUCTS).isEmpty())).isTrue();

        var storefront = api.get(scoped(path(PUBLIC_GROUPS, FEATURED_ITEMS), STORE_A), null);
        expect(storefront, HttpStatus.OK);
        assertThat(json(storefront).get(DESCRIPTION).get(NAME).asString()).isNotEmpty();

        assertThat(json(api.get(scoped(query(UNIQUE, String.format("code=%s", FEATURED_ITEMS)), STORE_A), admin))
                .get(EXISTS).asBoolean()).isTrue();

        // a code the store has not set up is an empty, inactive strip on the storefront surface, not an error
        String unknown = slug("nothing").toUpperCase();
        JsonNode unset = json(api.get(scoped(path(PUBLIC_GROUPS, unknown), STORE_A), null));
        assertThat(unset.get(ACTIVE).asBoolean()).isFalse();
        assertThat(unset.get(PRODUCTS)).isEmpty();
        // the console still learns that it does not exist
        expect(api.get(scoped(path(PRIVATE_GROUPS, unknown), STORE_A), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void aGroupCannotTakeAProductOfAnotherStore() {
        String code = slug(GRP).toUpperCase();
        // store B's products are not addressable from store A, so a save naming one is a 404 rather than a
        // silently empty group
        var refused = api.send(HttpMethod.POST, scoped(PRIVATE_GROUPS, STORE_A), admin,
                body(code, true, "999999", code));
        expect(refused, HttpStatus.NOT_FOUND);
        assertThat(json(refused).get(CODE).asString()).isEqualTo("CATALOG.PRODUCT.NOT_FOUND");
        expect(api.get(scoped(path(PRIVATE_GROUPS, code), STORE_A), admin), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.POST,
                scoped(path(PRIVATE_GROUPS, FEATURED_ITEMS, PRODUCT_SEGMENT, 999999L), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);
    }

    // -------------------------------------------------------------------------------------------- related items

    @Test
    void relatedItemsAreAProductsOwnGroup() {
        String related = scoped(path(V1_PRIVATE, PRODUCTS, PRODUCT_ONE, RELATIONSHIP, PRODUCT_TWO), STORE_A);
        expect(api.send(HttpMethod.POST, related, admin, null), HttpStatus.CREATED);
        // the group is created on first use, with the product as its parent
        JsonNode strip = json(api.get(scoped(path(V1, PRODUCTS, PRODUCT_ONE, RELATIONSHIP), STORE_A), null));
        assertThat(strip.get(CODE).asString()).isEqualTo("RELATED_ITEM");
        assertThat(strip.get(PARENT_PRODUCT).get(ID).asLong()).isEqualTo(PRODUCT_ONE);
        assertThat(strip.get(PRODUCTS)).hasSize(1);

        // adding the same one again does not duplicate it
        expect(api.send(HttpMethod.POST, related, admin, null), HttpStatus.CREATED);
        assertThat(json(api.get(scoped(path(V1, PRODUCTS, PRODUCT_ONE, RELATIONSHIP), STORE_A), null))
                .get(PRODUCTS)).hasSize(1);

        expect(api.send(HttpMethod.DELETE, related, admin, null), HttpStatus.NO_CONTENT);
        assertThat(json(api.get(scoped(path(V1, PRODUCTS, PRODUCT_ONE, RELATIONSHIP), STORE_A), null))
                .get(PRODUCTS)).isEmpty();

        // a product that has never had related items answers an empty, inactive strip: nothing to render, no error
        JsonNode none = json(api.get(scoped(path(V1, PRODUCTS, PRODUCT_THREE, RELATIONSHIP), STORE_A), null));
        assertThat(none.get(ACTIVE).asBoolean()).isFalse();
        assertThat(none.get(PRODUCTS)).isEmpty();
        expect(api.send(HttpMethod.DELETE,
                scoped(path(V1_PRIVATE, PRODUCTS, PRODUCT_THREE, RELATIONSHIP, PRODUCT_TWO), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.POST,
                scoped(path(V1_PRIVATE, PRODUCTS, 999999L, RELATIONSHIP, PRODUCT_TWO), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchTheGroup() {
        String code = slug("iso").toUpperCase();
        save(code, true, String.valueOf(PRODUCT_ONE));
        String other = api.token(ADMIN, STORE_B);

        expect(api.get(scoped(path(PRIVATE_GROUPS, code), STORE_A), other), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(PRIVATE_GROUPS, code), STORE_B), other), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_GROUPS, code), STORE_B), other, null),
                HttpStatus.NOT_FOUND);
        // nor from the storefront surface, which takes the store from the query string alone: store B reads an
        // empty strip under that code, never store A's members
        JsonNode fromB = json(api.get(scoped(path(PUBLIC_GROUPS, code), STORE_B), null));
        assertThat(fromB.get(ACTIVE).asBoolean()).isFalse();
        assertThat(fromB.get(PRODUCTS)).isEmpty();

        expect(api.send(HttpMethod.DELETE, scoped(path(PRIVATE_GROUPS, code), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
    }

    @Test
    void aModeratorAndAnAnonymousCallerAreRefusedTheConsole() {
        String moderator = api.token(MODERATOR, STORE_A);
        expect(api.get(scoped(PRIVATE_GROUPS, STORE_A), moderator), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(PRIVATE_GROUPS, STORE_A), moderator,
                body(slug("mod").toUpperCase(), true, "", "Mod")), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST,
                scoped(path(V1_PRIVATE, PRODUCTS, PRODUCT_ONE, RELATIONSHIP, PRODUCT_TWO), STORE_A), moderator,
                null), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(PRIVATE_GROUPS, STORE_A), null), HttpStatus.UNAUTHORIZED);
        expect(api.get(scoped(path(PUBLIC_GROUPS, FEATURED_ITEMS), STORE_A), null), HttpStatus.OK);
    }

}
