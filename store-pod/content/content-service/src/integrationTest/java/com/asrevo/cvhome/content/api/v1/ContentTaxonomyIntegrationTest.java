package com.asrevo.cvhome.content.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The taxonomies hanging off the content workflow — FAQ groups and post categories — plus the menu list and the
 * branding a peer service reads.
 *
 * <p>
 * These are the console's own lists rather than storefront content, and each is store-scoped, so every case here
 * carries its two obligations: a second store must not see the first store's rows, and a principal without
 * {@code CONTENT.MANAGE} must be refused rather than quietly succeeding. A taxonomy leaking across stores is how a
 * merchant learns their competitor's category names.
 * </p>
 */
@StorageIntegrationTest
class ContentTaxonomyIntegrationTest {

    /** Seeded store (languages ar, fr). */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String FAQ_GROUPS = path(PRIVATE, "faq", "groups");

    private static final String POST_CATEGORIES = path(PRIVATE, "posts", "categories");

    private static final String MENUS = path(PRIVATE, "menus");

    private static final String NAMES = "names";

    private static final String EN = "en";

    private static final String GROUP_BODY = """
            {"key":"%s","names":{"en":"%s"}}""";

    private static final String CATEGORY_BODY = """
            {"slug":"%s","names":{"en":"%s"}}""";

    private static final String ORDERING = "Ordering";

    private static final String SHIPPING = "Shipping";
    private static final String ORDERING_2 = "ordering";
    private static final String NOPE = "nope";
    private static final String NEWS = "news";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiTestSupport api;

    private String admin;

    private String moderator;

    private String otherStoreAdmin;

    @BeforeEach
    void setUp() {
        api = new ApiTestSupport(port, signer);
        admin = api.token(ROLE_STORE_ADMIN, STORE_A);
        moderator = api.token(ROLE_STORE_MODERATOR, STORE_A);
        otherStoreAdmin = api.token(ROLE_STORE_ADMIN, STORE_B);
    }

    @Test
    void afaqGroupIsCreatedListedRenamedAndDeleted() {
        String key = slug(ORDERING_2);
        long id = createGroup(key, ORDERING);

        assertThat(names(get(scoped(FAQ_GROUPS, STORE_A), admin), id)).isEqualTo(ORDERING);

        var renamed = api.send(HttpMethod.PUT, scoped(path(FAQ_GROUPS, id), STORE_A), admin,
                String.format(GROUP_BODY, key, SHIPPING));
        expect(renamed, HttpStatus.OK);
        assertThat(json(renamed).get(NAMES).get(EN).asString()).isEqualTo(SHIPPING);

        expect(api.send(HttpMethod.DELETE, scoped(path(FAQ_GROUPS, id), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        assertThat(idsOf(get(scoped(FAQ_GROUPS, STORE_A), admin))).doesNotContain(id);
    }

    @Test
    void onestoresFaqGroupsAreInvisibleToAnother() {
        long id = createGroup(slug(ORDERING_2), ORDERING);

        // The merchant's own category names are commercially theirs; the list must be scoped to the store.
        assertThat(idsOf(get(scoped(FAQ_GROUPS, STORE_B), otherStoreAdmin))).doesNotContain(id);
    }

    @Test
    void afaqGroupCannotBeReachedThroughAnotherStoresToken() {
        long id = createGroup(slug(ORDERING_2), ORDERING);

        var refused = api.send(HttpMethod.PUT, scoped(path(FAQ_GROUPS, id), STORE_B), otherStoreAdmin,
                String.format(GROUP_BODY, slug("theirs"), SHIPPING));

        assertThat(refused.getStatusCode().is2xxSuccessful()).isFalse();
    }

    @Test
    void amoderatorMayReadTheFaqGroupsButNotChangeThem() {
        long id = createGroup(slug(ORDERING_2), ORDERING);

        expect(get(scoped(FAQ_GROUPS, STORE_A), moderator), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, scoped(FAQ_GROUPS, STORE_A), moderator,
                String.format(GROUP_BODY, slug(NOPE), SHIPPING)), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, scoped(path(FAQ_GROUPS, id), STORE_A), moderator, null),
                HttpStatus.FORBIDDEN);
    }

    @Test
    void apostCategoryIsCreatedListedRenamedAndDeleted() {
        String categorySlug = slug(NEWS);
        long id = createCategory(categorySlug, ORDERING);

        assertThat(names(get(scoped(POST_CATEGORIES, STORE_A), admin), id)).isEqualTo(ORDERING);

        var renamed = api.send(HttpMethod.PUT, scoped(path(POST_CATEGORIES, id), STORE_A), admin,
                String.format(CATEGORY_BODY, categorySlug, SHIPPING));
        expect(renamed, HttpStatus.OK);
        assertThat(json(renamed).get(NAMES).get(EN).asString()).isEqualTo(SHIPPING);

        expect(api.send(HttpMethod.DELETE, scoped(path(POST_CATEGORIES, id), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        assertThat(idsOf(get(scoped(POST_CATEGORIES, STORE_A), admin))).doesNotContain(id);
    }

    @Test
    void onestoresPostCategoriesAreInvisibleToAnother() {
        long id = createCategory(slug(NEWS), ORDERING);

        assertThat(idsOf(get(scoped(POST_CATEGORIES, STORE_B), otherStoreAdmin))).doesNotContain(id);
    }

    @Test
    void amoderatorMayReadThePostCategoriesButNotChangeThem() {
        expect(get(scoped(POST_CATEGORIES, STORE_A), moderator), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, scoped(POST_CATEGORIES, STORE_A), moderator,
                String.format(CATEGORY_BODY, slug(NOPE), SHIPPING)), HttpStatus.FORBIDDEN);
    }

    @Test
    void thementListIsReadableAndScopedToTheStore() {
        var listed = get(scoped(MENUS, STORE_A), admin);

        expect(listed, HttpStatus.OK);
        assertThat(json(listed).isArray()).isTrue();
    }

    @Test
    void thementListIsRefusedWithoutAtoken() {
        assertThat(get(scoped(MENUS, STORE_A), null).getStatusCode().is2xxSuccessful()).isFalse();
    }

    private ResponseEntity<String> get(String url, String token) {
        return api.get(url, token);
    }

    private long createGroup(String key, String name) {
        var created = api.send(HttpMethod.POST, scoped(FAQ_GROUPS, STORE_A), admin,
                String.format(GROUP_BODY, key, name));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private long createCategory(String categorySlug, String name) {
        var created = api.send(HttpMethod.POST, scoped(POST_CATEGORIES, STORE_A), admin,
                String.format(CATEGORY_BODY, categorySlug, name));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private static java.util.List<Long> idsOf(ResponseEntity<String> response) {
        expect(response, HttpStatus.OK);
        java.util.List<Long> ids = new java.util.ArrayList<>();
        json(response).forEach(node -> ids.add(node.get(ID).asLong()));
        return ids;
    }

    private static String names(ResponseEntity<String> response, long id) {
        expect(response, HttpStatus.OK);
        for (JsonNode node : json(response)) {
            if (node.get(ID).asLong() == id) {
                return node.get(NAMES).get(EN).asString();
            }
        }
        throw new AssertionError(String.format("no row with id %d in the list", id));
    }

}
