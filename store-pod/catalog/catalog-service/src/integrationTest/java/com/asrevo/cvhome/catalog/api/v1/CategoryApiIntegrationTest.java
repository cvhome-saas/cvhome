package com.asrevo.cvhome.catalog.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CATEGORY;
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
 * The category tree over HTTP: the storefront reads, the console's CRUD, the lineage rewrites a move performs, and
 * the two cases every store-scoped endpoint owes — another store's token, and a role without manage rights.
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class CategoryApiIntegrationTest {

    private static final String HIERARCHY_SEGMENT = "category-hierarchy";

    private static final String ARABIC_SLUG = "%s-ar";

    private static final String LINEAGE_FORMAT = "/%d/%d/";

    private static final String CODE_QUERY = "code=%s";

    private static final String LANGUAGE = "language";

    private static final String RENAMED = "Renamed";

    private static final String HIERARCHY = path(V1, HIERARCHY_SEGMENT);

    private static final String PRIVATE_HIERARCHY = path(V1_PRIVATE, HIERARCHY_SEGMENT);

    private static final String PRIVATE_CATEGORY = path(V1_PRIVATE, CATEGORY);

    private static final String PUBLIC_CATEGORY = path(V1, CATEGORY);

    private static final String UNIQUE = path(PRIVATE_CATEGORY, "unique");

    private static final String VISIBLE = "visible";

    private static final String MOVE = "move";

    private static final String PARENT = "parent";

    private static final String CHILDREN = "children";

    private static final String LINEAGE = "lineage";

    private static final String DEPTH = "depth";

    private static final String MEN = "MEN";

    private static final String MEN_SLUG = "men";

    private static final String EN = "en";

    private static final String AR = "ar";

    /** Seeded root of store A's fashion tree; {@code /1/} is its lineage. */
    private static final long MEN_ID = 1L;

    private static final String DESCRIPTION_BODY =
            """
            {"%s":"%s","name":"%s","friendlyUrl":"%s","description":"d","title":"t"}""";

    private static final String CATEGORY_BODY =
            """
            {"code":"%s","sortOrder":3,"visible":true,"featured":false,%s"descriptions":[%s,%s]}""";

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

    private static String description(String language, String name, String friendlyUrl) {
        return String.format(DESCRIPTION_BODY, LANGUAGE, language, name, friendlyUrl);
    }

    private static String body(String code, String parent, String name) {
        String reference = parent == null ? "" : String.format("\"parent\":{\"code\":\"%s\"},", parent);
        String friendlyUrl = code.toLowerCase();
        return String.format(CATEGORY_BODY, code, reference, description(EN, name, friendlyUrl),
                description(AR, "قسم", String.format(ARABIC_SLUG, friendlyUrl)));
    }

    private static String item(String store, Object id, Object... more) {
        Object[] segments = new Object[more.length + 2];
        segments[0] = PRIVATE_CATEGORY;
        segments[1] = id;
        System.arraycopy(more, 0, segments, 2, more.length);
        return scoped(path(segments), store);
    }

    private long create(String code, String parent) {
        var created = api.send(HttpMethod.POST, scoped(PRIVATE_CATEGORY, STORE_A), admin, body(code, parent, code));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private JsonNode read(long id) {
        var response = api.get(item(STORE_A, id), admin);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    // ------------------------------------------------------------------------------------------ storefront reads

    @Test
    void storefrontReadsTheTreeAndOneCategoryBySlug() {
        JsonNode tree = json(api.get(scoped(HIERARCHY, STORE_A), null));
        assertThat(tree.get(CONTENT)).isNotEmpty();
        // the hierarchy nests: every seeded root carries its own children rather than a flat page
        assertThat(tree.get(CONTENT).valueStream().anyMatch(c -> !c.get(CHILDREN).isEmpty())).isTrue();

        var bySlug = api.get(scoped(path(PUBLIC_CATEGORY, MEN_SLUG), STORE_A), null);
        expect(bySlug, HttpStatus.OK);
        assertThat(json(bySlug).get(CODE).asString()).isEqualTo(MEN);
        // a public read answers the shopper's language only
        assertThat(json(bySlug).get(DESCRIPTIONS).isEmpty()).isTrue();

        expect(api.get(scoped(path(PUBLIC_CATEGORY, slug("nothing")), STORE_A), null), HttpStatus.NOT_FOUND);
    }

    @Test
    void aSlugOfAnotherStoreIsNotFoundHere() {
        // The storefront lookup is by slug alone, so the store scope is the only thing keeping one shop's urls out
        // of another's. A category created in store A must be invisible to store B's storefront.
        String code = slug("slugiso").toUpperCase();
        long id = create(code, null);
        String friendlyUrl = code.toLowerCase();
        expect(api.get(scoped(path(PUBLIC_CATEGORY, friendlyUrl), STORE_A), null), HttpStatus.OK);
        expect(api.get(scoped(path(PUBLIC_CATEGORY, friendlyUrl), STORE_B), null), HttpStatus.NOT_FOUND);
        // and its arabic slug resolves only when the shopper asks in arabic
        expect(api.get(scoped(path(PUBLIC_CATEGORY, String.format(ARABIC_SLUG, friendlyUrl)), STORE_A, AR), null),
                HttpStatus.OK);
        expect(api.get(scoped(path(PUBLIC_CATEGORY, String.format(ARABIC_SLUG, friendlyUrl)), STORE_A, EN), null),
                HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, item(STORE_A, id), admin, null), HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------------------------- console

    @Test
    void categoryLifecycleCreateReadUpdateHideDelete() {
        String code = slug("cat").toUpperCase();
        long id = create(code, MEN);

        JsonNode created = read(id);
        assertThat(created.get(CODE).asString()).isEqualTo(code);
        assertThat(created.get(PARENT).get(ID).asLong()).isEqualTo(MEN_ID);
        assertThat(created.get(LINEAGE).asString()).isEqualTo(String.format(LINEAGE_FORMAT, MEN_ID, id));
        assertThat(created.get(DEPTH).asInt()).isEqualTo(1);
        // a private read answers every language
        assertThat(created.get(DESCRIPTIONS)).hasSize(2);

        // exists is answered by code, per store
        assertThat(json(api.get(scoped(query(UNIQUE, String.format(CODE_QUERY, code)), STORE_A), admin))
                .get(EXISTS).asBoolean()).isTrue();
        assertThat(json(api.get(scoped(query(UNIQUE, String.format(CODE_QUERY, code)), STORE_B),
                api.token(ADMIN, STORE_B))).get(EXISTS).asBoolean()).isFalse();

        // an update merges descriptions by language: the english row keeps its id and takes the new name
        long descriptionId = created.get(DESCRIPTIONS).valueStream()
                .filter(d -> EN.equals(d.get(LANGUAGE).asString())).findFirst().orElseThrow().get(ID).asLong();
        expect(api.send(HttpMethod.PUT, item(STORE_A, id), admin, body(code, MEN, RENAMED)), HttpStatus.OK);
        JsonNode updated = read(id);
        JsonNode english = updated.get(DESCRIPTIONS).valueStream()
                .filter(d -> EN.equals(d.get(LANGUAGE).asString())).findFirst().orElseThrow();
        assertThat(english.get(NAME).asString()).isEqualTo(RENAMED);
        assertThat(english.get(ID).asLong()).isEqualTo(descriptionId);
        assertThat(updated.get(DESCRIPTIONS).valueStream()
                .anyMatch(d -> AR.equals(d.get(LANGUAGE).asString()))).isTrue();

        // the visibility switch is its own endpoint
        expect(api.send(HttpMethod.PATCH, item(STORE_A, id, VISIBLE), admin, "{\"code\":\"x\",\"visible\":false}"),
                HttpStatus.OK);
        assertThat(read(id).get(VISIBLE).asBoolean()).isFalse();

        expect(api.send(HttpMethod.DELETE, item(STORE_A, id), admin, null), HttpStatus.OK);
        expect(api.get(item(STORE_A, id), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void movingACategoryRewritesTheLineageOfEveryDescendant() {
        // The move endpoint recomputes the whole subtree: a child that kept its old lineage would drop out of
        // findSubtree and become invisible to every read that walks the tree.
        String parentCode = slug("par").toUpperCase();
        String childCode = slug("chi").toUpperCase();
        long parent = create(parentCode, null);
        long child = create(childCode, parentCode);
        assertThat(read(child).get(LINEAGE).asString()).isEqualTo(String.format(LINEAGE_FORMAT, parent, child));

        expect(api.send(HttpMethod.PUT, item(STORE_A, parent, MOVE, MEN_ID), admin, null), HttpStatus.OK);

        assertThat(read(parent).get(LINEAGE).asString()).isEqualTo(String.format(LINEAGE_FORMAT, MEN_ID, parent));
        assertThat(read(child).get(LINEAGE).asString())
                .isEqualTo(String.format("/%d/%d/%d/", MEN_ID, parent, child));
        assertThat(read(child).get(DEPTH).asInt()).isEqualTo(2);
        // the parent's read carries its subtree
        assertThat(read(parent).get(CHILDREN)).hasSize(1);

        // and back to the root: -1 is the root sentinel the console sends
        expect(api.send(HttpMethod.PUT, item(STORE_A, parent, MOVE, -1), admin, null), HttpStatus.OK);
        assertThat(read(parent).get(DEPTH).asInt()).isZero();

        expect(api.send(HttpMethod.DELETE, item(STORE_A, parent), admin, null), HttpStatus.OK);
        // deleting a node takes its descendants with it
        expect(api.get(item(STORE_A, child), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void listsAreFilteredByNameAndByProduct() {
        JsonNode all = json(api.get(scoped(PRIVATE_CATEGORY, STORE_A), admin));
        assertThat(all.get(CONTENT)).isNotEmpty();

        JsonNode named = json(api.get(scoped(query(PRIVATE_CATEGORY, "name=Men"), STORE_A), admin));
        assertThat(named.get(CONTENT)).isNotEmpty();
        assertThat(named.get(CONTENT).size()).isLessThan(all.get(CONTENT).size());

        expect(api.get(scoped(PRIVATE_HIERARCHY, STORE_A), admin), HttpStatus.OK);

        // a seeded product sits in at least one category
        JsonNode ofProduct = json(api.get(scoped(path(PRIVATE_CATEGORY, "product", 1), STORE_A), admin));
        assertThat(ofProduct.get(CONTENT)).isNotEmpty();
    }

    @Test
    void anUnresolvableParentIsRefused() {
        var refused = api.send(HttpMethod.POST, scoped(PRIVATE_CATEGORY, STORE_A), admin,
                body(slug("orphan").toUpperCase(), "NO_SUCH_PARENT", "Orphan"));
        expect(refused, HttpStatus.BAD_REQUEST);
        assertThat(json(refused).get(CODE).asString()).isEqualTo("CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE");

        // an empty code never passes validation
        expect(api.send(HttpMethod.POST, scoped(PRIVATE_CATEGORY, STORE_A), admin,
                body("", null, "No code")), HttpStatus.BAD_REQUEST);

        expect(api.get(item(STORE_A, 999999L), admin), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, item(STORE_A, 999999L, MOVE, MEN_ID), admin, null), HttpStatus.NOT_FOUND);
    }

    // ---------------------------------------------------------------------------------- tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchTheCategory() {
        String code = slug("iso").toUpperCase();
        long id = create(code, null);
        String other = api.token(ADMIN, STORE_B);

        // asking for store A with a store-B token is refused outright
        expect(api.get(item(STORE_A, id), other), HttpStatus.FORBIDDEN);
        // and store A's id simply does not exist inside store B
        expect(api.get(item(STORE_B, id), other), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, item(STORE_B, id), other, body(code, null, "Hijack")), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, item(STORE_B, id), other, null), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, item(STORE_A, id), admin, null), HttpStatus.OK);
    }

    @Test
    void aModeratorAndAnAnonymousCallerAreRefusedTheConsole() {
        String moderator = api.token(MODERATOR, STORE_A);
        ResponseEntity<String> read = api.get(scoped(PRIVATE_CATEGORY, STORE_A), moderator);
        expect(read, HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(PRIVATE_CATEGORY, STORE_A), moderator,
                body(slug("mod").toUpperCase(), null, "Mod")), HttpStatus.FORBIDDEN);
        expect(api.get(scoped(PRIVATE_CATEGORY, STORE_A), null), HttpStatus.UNAUTHORIZED);
        // the storefront surface stays open
        expect(api.get(scoped(HIERARCHY, STORE_A), null), HttpStatus.OK);
    }

}
