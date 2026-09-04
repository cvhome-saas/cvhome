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
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CODE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.NAME;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.query;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The option vocabulary over HTTP: whole-document writes with value identity, code uniqueness per store, tenant
 * isolation and the permission gate.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductOptionApiIntegrationTest {

    private static final String UNIQUE = "unique";
    private static final String CODE_S = "code=%s";
    private static final String PRODUCT = "product";

    private static final String OPTION = "option";

    private static final String OPTIONS = "options";

    private static final String VALUES = "values";

    private static final String EN = "en";

    private static final String BODY = """
            {"code":"%s","sortOrder":0,
             "descriptions":[{"language":"en","name":"Color"},{"language":"fr","name":"Couleur"}],
             "values":[
               {"code":"red","sortOrder":0,"descriptions":[{"language":"en","name":"Red"}]},
               {"code":"blue","sortOrder":1,"descriptions":[{"language":"en","name":"Blue"}]}]}""";

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

    private long create(String code) {
        var created = api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, PRODUCT, OPTION), STORE_A), admin,
                BODY.formatted(code));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    @Test
    void createReadEditDeleteRoundTrip() {
        String code = slug("color");
        long id = create(code);

        var read = api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A, "fr"), admin);
        expect(read, HttpStatus.OK);
        JsonNode option = json(read);
        assertThat(option.get(CODE).asString()).isEqualTo(code);
        assertThat(option.get(NAME).asString()).isEqualTo("Couleur");
        assertThat(option.get(VALUES)).hasSize(2);
        assertThat(option.get(VALUES).get(0).get(CODE).asString()).isEqualTo("red");
        long redId = option.get(VALUES).get(0).get(ID).asLong();

        // edit: keep red by id under a new code, drop blue, add green
        String edit = """
                {"code":"%s","sortOrder":0,"descriptions":[{"language":"en","name":"Colour"}],
                 "values":[
                   {"id":%d,"code":"crimson","sortOrder":0,"descriptions":[{"language":"en","name":"Crimson"}]},
                   {"code":"green","sortOrder":1,"descriptions":[{"language":"en","name":"Green"}]}]}"""
                .formatted(code, redId);
        expect(api.send(HttpMethod.PUT, scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A), admin, edit),
                HttpStatus.OK);

        JsonNode edited = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A, EN), admin));
        assertThat(edited.get(VALUES)).hasSize(2);
        assertThat(edited.get(VALUES).get(0).get(ID).asLong()).as("id-addressed value keeps its row")
                .isEqualTo(redId);
        assertThat(edited.get(VALUES).get(0).get(CODE).asString()).isEqualTo("crimson");
        assertThat(edited.get(VALUES).get(1).get(CODE).asString()).isEqualTo("green");

        expect(api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A), admin, null),
                HttpStatus.OK);
        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void codeIsUniquePerStoreAndDuplicateValueCodesAreRefused() {
        String code = slug("size");
        create(code);

        var duplicate = api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, PRODUCT, OPTION), STORE_A), admin,
                BODY.formatted(code));
        expect(duplicate, HttpStatus.CONFLICT);

        String duplicateValues = """
                {"code":"%s","descriptions":[{"language":"en","name":"Dup"}],
                 "values":[{"code":"one","descriptions":[{"language":"en","name":"One"}]},
                           {"code":"one","descriptions":[{"language":"en","name":"One again"}]}]}"""
                .formatted(slug("dup"));
        expect(api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, PRODUCT, OPTION), STORE_A), admin,
                duplicateValues), HttpStatus.CONFLICT);

        // the same code is free in another store
        String otherAdmin = api.token(ADMIN, STORE_B);
        expect(api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, PRODUCT, OPTION), STORE_B), otherAdmin,
                BODY.formatted(code)), HttpStatus.CREATED);
    }

    @Test
    void anOptionAProductStillVariesByCannotBeDeleted() {
        /*
         * The delete guard. Store A's seed assigns colour (option 1) to a product and its variants sell that
         * option's values, so the vocabulary entry is load-bearing: deleting it would orphan those variants,
         * and the pod refuses with a typed 409 the console surfaces as a named toast rather than a generic
         * conflict. The option must survive the refusal.
         */
        var refused = api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A),
                admin, null);
        expect(refused, HttpStatus.CONFLICT);

        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A), admin), HttpStatus.OK);
    }

    @Test
    void anEditThatDropsAValueAVariantSellsByIsRefusedTheSameWayADeleteIs() {
        /*
         * The other door onto the same rule. An update replaces the whole value set, so a value the merchant
         * leaves out is orphan-removed — and `fk_pvov_value` carries no `ON DELETE`, so dropping one a variant
         * still points at used to surface as a raw foreign-key 500 while the neighbouring DELETE answered a
         * named 409. Store A's seed sells colour (option 1) by both red and blue, so re-sending it with only
         * red is exactly that case.
         */
        String dropBlue = """
                {"code":"color","sortOrder":0,
                 "descriptions":[{"language":"en","name":"Color"}],
                 "values":[{"id":1,"code":"red","sortOrder":0,
                            "descriptions":[{"language":"en","name":"Red"}]}]}""";

        expect(api.send(HttpMethod.PUT, scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A), admin,
                dropBlue), HttpStatus.CONFLICT);

        // and the value is still there to be sold by
        JsonNode option = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A), admin));
        assertThat(option.get(VALUES).valueStream().map(value -> value.get(CODE).asString()).toList())
                .contains("blue");
    }

    @Test
    void resendingAnOptionsWholeValueSetIsAllowedEvenWhileVariantsSellThem() {
        /*
         * The other side of the edit guard: it must refuse only *removals*. Colour (option 1) is sold by the
         * seeded variants, so if the guard keyed on "is this option in use" rather than "is this value being
         * dropped", every in-use option would have become read-only.
         *
         * Re-sends the set exactly as seeded — five values, same codes, same names — so the case proves the
         * write is allowed without renaming anything: the vocabulary is shared, and
         * `ProductVariantApiIntegrationTest` asserts on "Red" by name.
         */
        JsonNode before = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A, EN), admin));
        assertThat(before.get(VALUES)).hasSize(5);

        StringBuilder values = new StringBuilder();
        for (JsonNode value : before.get(VALUES)) {
            values.append(values.isEmpty() ? "" : ",")
                    .append("{\"id\":%d,\"code\":\"%s\",\"sortOrder\":%d,\"descriptions\":[{\"language\":\"en\",\"name\":\"%s\"}]}"
                            .formatted(value.get(ID).asLong(), value.get(CODE).asString(),
                                    value.get("sortOrder").asInt(), value.get(NAME).asString()));
        }
        String resend = "{\"code\":\"color\",\"sortOrder\":0,\"descriptions\":[{\"language\":\"en\",\"name\":\"%s\"}],\"values\":[%s]}"
                .formatted(before.get(NAME).asString(), values);

        expect(api.send(HttpMethod.PUT, scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A), admin, resend),
                HttpStatus.OK);

        JsonNode after = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, 1L), STORE_A, EN), admin));
        assertThat(after.get(VALUES)).hasSize(5);
        assertThat(after.get(VALUES).get(0).get(NAME).asString()).isEqualTo("Red");
    }

    @Test
    void anOptionThatDoesNotExistAnswersNotFound() {
        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, 999999L), STORE_A), admin),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void anotherStoreCannotSeeOrTouchThisStoresOption() {
        long id = create(slug("iso"));
        String otherAdmin = api.token(ADMIN, STORE_B);

        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A), otherAdmin),
                HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_B), otherAdmin),
                HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_B), otherAdmin,
                null), HttpStatus.NOT_FOUND);

        JsonNode listed = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTIONS), STORE_B), otherAdmin));
        for (JsonNode option : listed.get("content")) {
            assertThat(option.get(ID).asLong()).isNotEqualTo(id);
        }
    }

    @Test
    void moderatorAndAnonymousAreRejected() {
        String moderator = api.token(MODERATOR, STORE_A);
        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTIONS), STORE_A), moderator), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(path(V1_PRIVATE, PRODUCT, OPTION), STORE_A), moderator,
                BODY.formatted(slug("mod"))), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTIONS), STORE_A), null), HttpStatus.UNAUTHORIZED);
    }

    /**
     * The pre-flight the console runs before saving a new option.
     *
     * <p>
     * Here {@code exists} means literally that — the code is already taken. Note that content's identically shaped
     * {@code slug-available} endpoint returns the <em>opposite</em> sense (its {@code exists} means the slug is
     * free), so the two must not be read as one convention.
     * </p>
     *
     * <p>
     * The case with teeth is the last one: a code taken in another store must read as free here, or one merchant's
     * option codes would constrain another's — and, read the other way, would tell them what their competitor uses.
     * </p>
     */
    @Test
    void theCodePreFlightIsScopedToTheStoreAskingIt() {
        String code = slug("preflight");
        create(code);

        assertThat(exists(STORE_A, code)).isTrue();
        assertThat(exists(STORE_A, slug("never-used"))).isFalse();
        assertThat(exists(STORE_B, code)).isFalse();
    }

    @Test
    void theCodePreFlightIsRefusedToAmoderator() {
        expect(api.get(query(scoped(path(V1_PRIVATE, PRODUCT, OPTION, UNIQUE), STORE_A),
                String.format(CODE_S, slug("x"))), api.token(MODERATOR, STORE_A)), HttpStatus.FORBIDDEN);
    }

    private boolean exists(String store, String code) {
        var response = api.get(query(scoped(path(V1_PRIVATE, PRODUCT, OPTION, UNIQUE), store),
                String.format(CODE_S, code)), api.token(ADMIN, store));
        expect(response, HttpStatus.OK);
        return json(response).get("exists").asBoolean();
    }

}
