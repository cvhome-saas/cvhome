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

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CODE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PUBLISHED;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STATUS;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STOREFRONT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Home-page sections, end to end.
 *
 * <p>
 * The home page used to be four product groups hard-coded in the storefront's loader, so a seller could not
 * reorder it, retitle it or put anything else on it. These are the two things that has to buy them: a block they
 * created appears, and the order they set is the order it renders in.
 * </p>
 */
@StorageIntegrationTest
class SectionApiIntegrationTest {

    /** A seeded store. */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store — how tenant isolation is proven rather than assumed. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String SECTIONS = path(PRIVATE, "sections");

    private static final String REORDER = path(SECTIONS, "reorder");

    private static final String HOME_SECTIONS = "%s/home-sections?store=%s&lang=%s";

    private static final String PUBLISH = "publish";

    private static final String EN = "en";

    private static final String FEATURED = "FEATURED_ITEMS";

    private static final String SEC = "sec";

    private static final String PRODUCT_GROUP = "PRODUCT_GROUP";

    private static final String ID_PAIR = "[%d,%d]";

    private static final String MINE = "Mine";

    private static final String SECTION_BODY = """
            {"slug":"%s","kind":"%s"%s,
             "translations":[{"language":"en","title":"%s"},
                             {"language":"ar","title":"قسم"}]}""";

    private static final String TARGET_VALUE = "targetValue";

    private static final String SORT_ORDER = "sortOrder";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiTestSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiTestSupport(port, signer);
        admin = api.token(ROLE_STORE_ADMIN, STORE_A);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static String body(String slug, String kind, String target, String title) {
        String t = target == null ? "" : String.format(",\"%s\":\"%s\"", TARGET_VALUE, target);
        return String.format(SECTION_BODY, slug, kind, t, title);
    }

    private long create(String store, String token, String body) {
        var created = api.send(HttpMethod.POST, scoped(SECTIONS, store), token, body);
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private ResponseEntity<String> publish(String store, long id) {
        return api.send(HttpMethod.POST, scoped(path(SECTIONS, id, PUBLISH), store),
                api.token(ROLE_STORE_ADMIN, store), null);
    }

    private JsonNode home(String store) {
        return json(api.get(String.format(HOME_SECTIONS, STOREFRONT, store, EN), null));
    }

    /** A published `PRODUCT_GROUP` rail, which is the common case and the one the seeds replace. */
    private long rail(String store, String title, String target) {
        long id = create(store, api.token(ROLE_STORE_ADMIN, store), body(slug(SEC), PRODUCT_GROUP, target, title));
        expect(publish(store, id), HttpStatus.OK);
        return id;
    }

    // ------------------------------------------------------------------------------------------------- cases

    @Test
    void aPublishedProductGroupSectionReachesTheHomePage() {
        String title = "Featured this week";

        long id = rail(STORE_A, title, FEATURED);

        JsonNode sections = home(STORE_A);
        JsonNode mine = null;
        for (JsonNode s : sections) {
            if (s.get(ID).asLong() == id) {
                mine = s;
            }
        }
        assertThat(mine).isNotNull();
        assertThat(mine.get("kind").asString()).isEqualTo(PRODUCT_GROUP);
        assertThat(mine.get(TARGET_VALUE).asString()).isEqualTo(FEATURED);
        assertThat(mine.get(ApiTestSupport.TITLE).asString()).isEqualTo(title);
    }

    /**
     * A draft is not a hidden block on a live page. The storefront read is the only one that matters here: the
     * console lists drafts on purpose.
     */
    @Test
    void anUnpublishedSectionIsNotOnTheHomePage() {
        long id = create(STORE_A, admin, body(slug(SEC), PRODUCT_GROUP, FEATURED, "Draft rail"));

        for (JsonNode s : home(STORE_A)) {
            assertThat(s.get(ID).asLong()).isNotEqualTo(id);
        }
    }

    /**
     * The whole order in one request, and every section renumbered from it — which is what stops two blocks
     * sharing a position and rendering in whichever order the read happened to return them.
     */
    @Test
    void reorderingRewritesThePageAndTheStorefrontFollows() {
        long first = rail(STORE_A, "First", FEATURED);
        long second = rail(STORE_A, "Second", "NEW_ARRIVALS");

        // 204: the reorder writes and answers nothing — the caller already knows the order it sent.
        expect(api.send(HttpMethod.PATCH, scoped(REORDER, STORE_A), admin,
                String.format(ID_PAIR, second, first)), HttpStatus.NO_CONTENT);

        JsonNode sections = home(STORE_A);
        Integer secondOrder = null;
        Integer firstOrder = null;
        for (JsonNode s : sections) {
            if (s.get(ID).asLong() == second) {
                secondOrder = s.get(SORT_ORDER).asInt();
            }
            if (s.get(ID).asLong() == first) {
                firstOrder = s.get(SORT_ORDER).asInt();
            }
        }
        assertThat(secondOrder).isNotNull();
        assertThat(firstOrder).isNotNull();
        assertThat(secondOrder).isLessThan(firstOrder);
    }

    /**
     * A block that renders nothing is worse than a missing one: it reaches the page as a gap whose cause the
     * seller cannot see. The kinds that collect something are refused without a target.
     */
    @Test
    void publishingASectionWithNothingToPointAtIsRefused() {
        long id = create(STORE_A, admin, body(slug(SEC), PRODUCT_GROUP, null, "Points nowhere"));

        var refused = publish(STORE_A, id);

        expect(refused, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(refused).get(CODE).asString()).isEqualTo("CONTENT.PUBLISH.INCOMPLETE");
        assertThat(refused.getBody()).contains(TARGET_VALUE);
    }

    /**
     * `RICH_TEXT` carries its own copy, so it has nothing to point at and publishes as it is. Worth stating
     * next to the case above: the target rule is per kind, not a blanket one.
     */
    @Test
    void aKindThatCarriesItsOwnCopyNeedsNoTarget() {
        long id = create(STORE_A, admin, body(slug(SEC), "RICH_TEXT", null, "About us"));

        var published = publish(STORE_A, id);

        expect(published, HttpStatus.OK);
        assertThat(json(published).get(STATUS).asString()).isEqualTo(PUBLISHED);
    }

    @Test
    void aModeratorCannotCreateASection() {
        expect(api.send(HttpMethod.POST, scoped(SECTIONS, STORE_A), api.token(ROLE_STORE_MODERATOR, STORE_A),
                body(slug(SEC), PRODUCT_GROUP, FEATURED, "Nope")), HttpStatus.FORBIDDEN);
    }

    @Test
    void oneStoresSectionsAreNotOnAnothersHomePage() {
        long mine = rail(STORE_A, MINE, FEATURED);

        for (JsonNode s : home(STORE_B)) {
            assertThat(s.get(ID).asLong()).isNotEqualTo(mine);
        }
    }

    /**
     * Reorder names an id from another store. It has to be refused rather than silently skipped — a request the
     * server half-applies leaves the page in an order the seller did not ask for and cannot see.
     */
    @Test
    void reorderRefusesAnIdThatIsNotThisStoresSection() {
        long mine = rail(STORE_A, MINE, FEATURED);
        long theirs = rail(STORE_B, "Theirs", FEATURED);

        expect(api.send(HttpMethod.PATCH, scoped(REORDER, STORE_A), admin,
                String.format(ID_PAIR, mine, theirs)), HttpStatus.NOT_FOUND);
    }

}
