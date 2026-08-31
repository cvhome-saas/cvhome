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

    private static final String PRODUCT = "product";

    private static final String OPTION = "option";

    private static final String OPTIONS = "options";

    private static final String VALUES = "values";

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

        JsonNode edited = json(api.get(scoped(path(V1_PRIVATE, PRODUCT, OPTION, id), STORE_A, "en"), admin));
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
}
