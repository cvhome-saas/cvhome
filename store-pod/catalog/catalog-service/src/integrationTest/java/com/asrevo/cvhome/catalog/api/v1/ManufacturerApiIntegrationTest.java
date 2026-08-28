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
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.DESCRIPTIONS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.EXISTS;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.NAME;
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
 * Brands and product types — the two console-managed reference lists a product points at, plus the storefront's
 * brand facet, which is derived from the products actually visible in a category subtree.
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ManufacturerApiIntegrationTest {

    private static final String MANUFACTURER_SEGMENT = "manufacturer";

    private static final String UNIQUE_SEGMENT = "unique";

    private static final String PRODUCT_SEGMENT = "product";

    private static final String TYPE_SEGMENT = "type";

    private static final String RENAMED = "Renamed";

    private static final String CATEGORY_SEGMENT = "category";

    private static final String MANUFACTURERS = path(V1_PRIVATE, "manufacturers");

    private static final String MANUFACTURER = path(V1_PRIVATE, MANUFACTURER_SEGMENT);

    private static final String UNIQUE_BRAND = path(MANUFACTURER, UNIQUE_SEGMENT);

    private static final String TYPES = path(V1_PRIVATE, PRODUCT_SEGMENT, "types");

    private static final String TYPE = path(V1_PRIVATE, PRODUCT_SEGMENT, TYPE_SEGMENT);

    private static final String UNIQUE_TYPE = path(TYPE, UNIQUE_SEGMENT);

    private static final String CODE_QUERY = "code=%s";

    private static final String NIKE = "NIKE";

    /** Seeded category MEN of store A; its subtree holds the visible products the facet is built from. */
    private static final long MEN_ID = 1L;

    private static final String BRAND_BODY = """
            {"code":"%s","order":2,
             "descriptions":[{"language":"en","name":"%s","title":"T","description":"d","friendlyUrl":"%s"},
                             {"language":"ar","name":"علامة"}]}""";

    private static final String TYPE_BODY = """
            {"code":"%s","allowAddToCart":true,"visible":true,
             "descriptions":[{"language":"en","name":"%s","title":"T","description":"d"}]}""";

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

    private static String brand(String code, String name) {
        return String.format(BRAND_BODY, code, name, code.toLowerCase());
    }

    private long createBrand(String code) {
        var created = api.send(HttpMethod.POST, scoped(MANUFACTURER, STORE_A), admin, brand(code, code));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private JsonNode readBrand(long id) {
        var response = api.get(scoped(path(MANUFACTURER, id), STORE_A), admin);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    // --------------------------------------------------------------------------------------------------- brands

    @Test
    void brandLifecycleCreateReadUpdateDelete() {
        String code = slug("brand").toUpperCase();
        long id = createBrand(code);

        JsonNode created = readBrand(id);
        assertThat(created.get(CODE).asString()).isEqualTo(code);
        assertThat(created.get("order").asInt()).isEqualTo(2);
        assertThat(created.get(DESCRIPTIONS)).hasSize(2);

        assertThat(json(api.get(scoped(query(UNIQUE_BRAND, String.format(CODE_QUERY, code)), STORE_A), admin))
                .get(EXISTS).asBoolean()).isTrue();

        // the list narrows on a name fragment in any language
        JsonNode named = json(api.get(scoped(query(MANUFACTURERS, String.format("name=%s", code)), STORE_A), admin));
        assertThat(named.get(CONTENT)).hasSize(1);
        assertThat(json(api.get(scoped(MANUFACTURERS, STORE_A), admin)).get(CONTENT).size())
                .isGreaterThan(named.get(CONTENT).size());

        // the update merges by language, so the english row keeps its id
        long descriptionId = created.get(DESCRIPTIONS).get(0).get(ID).asLong();
        expect(api.send(HttpMethod.PUT, scoped(path(MANUFACTURER, id), STORE_A), admin, brand(code, RENAMED)),
                HttpStatus.OK);
        JsonNode updated = readBrand(id);
        assertThat(updated.get(DESCRIPTIONS).valueStream().anyMatch(d -> d.get(ID).asLong() == descriptionId))
                .isTrue();
        assertThat(updated.get(DESCRIPTIONS).valueStream()
                .anyMatch(d -> RENAMED.equals(d.get(NAME).asString()))).isTrue();

        // and the renamed brand is found under its new name
        assertThat(json(api.get(scoped(query(MANUFACTURERS, "name=Renamed"), STORE_A), admin)).get(CONTENT))
                .hasSize(1);

        expect(api.send(HttpMethod.DELETE, scoped(path(MANUFACTURER, id), STORE_A), admin, null), HttpStatus.OK);
        expect(api.get(scoped(path(MANUFACTURER, id), STORE_A), admin), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(path(MANUFACTURER, id), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void theStorefrontBrandFacetComesFromTheCategorySubtree() {
        var facet = api.get(scoped(path(V1, CATEGORY_SEGMENT, MEN_ID, MANUFACTURER_SEGMENT), STORE_A), null);
        expect(facet, HttpStatus.OK);
        JsonNode brands = json(facet);
        assertThat(brands).isNotEmpty();
        assertThat(brands.valueStream().anyMatch(b -> NIKE.equals(b.get(CODE).asString()))).isTrue();
        // a facet is a shopper-facing list: one language, no full description set
        assertThat(brands.get(0).get(DESCRIPTIONS).isEmpty()).isTrue();

        expect(api.get(scoped(path(V1, CATEGORY_SEGMENT, 999999L, MANUFACTURER_SEGMENT), STORE_A), null), HttpStatus.NOT_FOUND);
    }

    // ---------------------------------------------------------------------------------------------- product types

    @Test
    void productTypeLifecycleCreateReadUpdateDeleteAndRefuseDuplicates() {
        String code = slug(TYPE_SEGMENT).toUpperCase();
        var created = api.send(HttpMethod.POST, scoped(TYPE, STORE_A), admin, String.format(TYPE_BODY, code, code));
        expect(created, HttpStatus.CREATED);
        long id = json(created).get(ID).asLong();

        JsonNode read = json(api.get(scoped(path(TYPE, id), STORE_A), admin));
        assertThat(read.get(CODE).asString()).isEqualTo(code);
        assertThat(read.get("allowAddToCart").asBoolean()).isTrue();
        assertThat(read.get(DESCRIPTIONS)).hasSize(1);

        assertThat(json(api.get(scoped(query(UNIQUE_TYPE, String.format(CODE_QUERY, code)), STORE_A), admin))
                .get(EXISTS).asBoolean()).isTrue();

        // the code is unique per store: a second create with the same code is a conflict
        var duplicate = api.send(HttpMethod.POST, scoped(TYPE, STORE_A), admin,
                String.format(TYPE_BODY, code, code));
        expect(duplicate, HttpStatus.CONFLICT);
        assertThat(json(duplicate).get(CODE).asString()).isEqualTo("CATALOG.PRODUCT_TYPE.DUPLICATE");

        expect(api.send(HttpMethod.PUT, scoped(path(TYPE, id), STORE_A), admin,
                String.format(TYPE_BODY, code, RENAMED)), HttpStatus.OK);
        assertThat(json(api.get(scoped(path(TYPE, id), STORE_A), admin)).get(DESCRIPTIONS).get(0).get(NAME)
                .asString()).isEqualTo(RENAMED);

        assertThat(json(api.get(scoped(TYPES, STORE_A), admin)).get(CONTENT)).isNotEmpty();

        expect(api.send(HttpMethod.DELETE, scoped(path(TYPE, id), STORE_A), admin, null), HttpStatus.OK);
        expect(api.get(scoped(path(TYPE, id), STORE_A), admin), HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchTheBrandOrType() {
        String code = slug("iso").toUpperCase();
        long id = createBrand(code);
        String other = api.token(ADMIN, STORE_B);

        expect(api.get(scoped(path(MANUFACTURER, id), STORE_A), other), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(MANUFACTURER, id), STORE_B), other), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, scoped(path(MANUFACTURER, id), STORE_B), other, brand(code, "Hijack")),
                HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(path(MANUFACTURER, id), STORE_B), other, null),
                HttpStatus.NOT_FOUND);
        assertThat(json(api.get(scoped(query(UNIQUE_BRAND, String.format(CODE_QUERY, code)), STORE_B), other))
                .get(EXISTS).asBoolean()).isFalse();
        // store A's product types are equally invisible
        expect(api.get(scoped(path(TYPE, 1L), STORE_B), other), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(MANUFACTURER, id), STORE_A), admin, null), HttpStatus.OK);
    }

    @Test
    void aModeratorAndAnAnonymousCallerAreRefusedTheConsole() {
        String moderator = api.token(MODERATOR, STORE_A);
        expect(api.get(scoped(MANUFACTURERS, STORE_A), moderator), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(MANUFACTURER, STORE_A), moderator,
                brand(slug("mod").toUpperCase(), "Mod")), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(TYPES, STORE_A), moderator), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(MANUFACTURERS, STORE_A), null), HttpStatus.UNAUTHORIZED);
        // the storefront facet stays open
        expect(api.get(scoped(path(V1, CATEGORY_SEGMENT, MEN_ID, MANUFACTURER_SEGMENT), STORE_A), null), HttpStatus.OK);
    }

}
