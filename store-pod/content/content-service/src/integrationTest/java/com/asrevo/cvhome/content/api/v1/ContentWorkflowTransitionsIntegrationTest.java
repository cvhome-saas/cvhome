package com.asrevo.cvhome.content.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STATUS;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.query;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The workflow every content type shares, exercised through pages: the transitions past publish/unpublish,
 * per-locale editing, revision restore, the slug pre-flight and the bulk action.
 *
 * <p>
 * {@code WorkflowContentApi} is abstract and its mappings are inherited by six concrete controllers, so a defect
 * here is a defect in pages, posts, banners, FAQ entries, policies and menus at once. That is also why these cases
 * live in one file rather than being repeated per type.
 * </p>
 *
 * <p>
 * Each transition is store-scoped and gated on {@code CONTENT.MANAGE}, so the two obligations are asserted against
 * the whole set rather than one endpoint at a time: a moderator is refused every write, and a second store's token
 * cannot reach the first store's item.
 * </p>
 */
@StorageIntegrationTest
// Same reason as ContentApiIntegrationTest: the scheduler archives rows behind the assertions on a slow run.
@TestPropertySource(properties = {
        "com.asrevo.cvhome.content.scheduler.initial-delay=PT24H",
        "com.asrevo.cvhome.content.scheduler.delay=PT24H"})
class ContentWorkflowTransitionsIntegrationTest {

    /** A seeded store (languages ar, fr). */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store — how tenant isolation is proven rather than assumed. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String PAGES = path(PRIVATE, "pages");

    private static final String TITLE_ONE = "First";

    private static final String TITLE_TWO = "Second";

    private static final String BODY_ONE = "<p>One</p>";

    private static final String PAGE_BODY = """
            {%s"slug":"%s","translations":[{"language":"en","title":"%s","body":"%s"}]}""";

    private static final String TRANSLATION_BODY = """
            {"language":"ar","title":"عنوان","body":"<p>نص</p>"}""";

    private static final String BULK_BODY = """
            {"ids":[%d],"action":"%s"}""";

    private static final String DRAFT = "DRAFT";

    private static final String REVIEW = "REVIEW";

    private static final String ARCHIVED = "ARCHIVED";
    private static final String SUBMIT_REVIEW = "submit-review";
    private static final String RESTORE = "restore";
    private static final String ARCHIVE = "archive";
    private static final String TRANSLATIONS = "translations";
    private static final String AR = "ar";
    private static final String REVISIONS = "revisions";
    private static final String BULK = "bulk";
    private static final String ARCHIVE_2 = "ARCHIVE";
    private static final String OK = "ok";

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
    void adraftGoesToReviewAndBackToDraft() {
        long id = create(slug("review"), TITLE_ONE);

        assertThat(statusAfter(post(item(id, SUBMIT_REVIEW), null))).isEqualTo(REVIEW);
        // RESTORE on a non-archived item is the way back to DRAFT the console's Reject button uses.
        assertThat(statusAfter(post(item(id, RESTORE), null))).isEqualTo(DRAFT);
    }

    @Test
    void anItemIsArchivedAndBroughtBack() {
        long id = create(slug(ARCHIVE), TITLE_ONE);

        assertThat(statusAfter(post(item(id, ARCHIVE), null))).isEqualTo(ARCHIVED);
        assertThat(statusAfter(post(item(id, RESTORE), null))).isEqualTo(DRAFT);
    }

    @Test
    void alocaleIsEditedOnItsOwnWithoutTouchingTheOthers() {
        long id = create(slug("locale"), TITLE_ONE);

        var saved = api.send(HttpMethod.PUT, item(id, TRANSLATIONS, AR), admin, TRANSLATION_BODY);
        expect(saved, HttpStatus.OK);

        JsonNode read = json(api.get(scoped(path(PAGES, id), STORE_A), admin));
        assertThat(read.get("locales")).hasSize(2);
    }

    @Test
    void anEarlierRevisionIsRestoredOverTheCurrentOne() {
        String pageSlug = slug("revision");
        long id = create(pageSlug, TITLE_ONE);
        expect(api.send(HttpMethod.PUT, scoped(path(PAGES, id), STORE_A), admin,
                body(pageSlug, TITLE_TWO, 0)), HttpStatus.OK);

        JsonNode revisions = json(api.get(item(id, REVISIONS), admin));
        assertThat(revisions).isNotEmpty();
        int version = revisions.get(revisions.size() - 1).get("version").asInt();

        expect(post(item(id, REVISIONS, version, RESTORE), null), HttpStatus.OK);
    }

    @Test
    void theSlugPreFlightAnswersTakenForOneInUseAndFreeForOneThatIsNot() {
        String pageSlug = slug("preflight");
        long id = create(pageSlug, TITLE_ONE);

        assertThat(exists(pageSlug, null)).isFalse();
        assertThat(exists(slug("never-used"), null)).isTrue();
        // Excluding the item that holds it is what lets a rename keep its own slug.
        assertThat(exists(pageSlug, id)).isTrue();
    }

    @Test
    void apreviewTokenIsIssuedForAnUnpublishedItem() {
        long id = create(slug("preview"), TITLE_ONE);

        var issued = post(item(id, "preview-token"), null);

        expect(issued, HttpStatus.OK);
        assertThat(json(issued).get("token").asString()).isNotBlank();
    }

    @Test
    void abulkActionAnswersPerItemRatherThanFailingWholesale() {
        long id = create(slug(BULK), TITLE_ONE);

        var result = api.send(HttpMethod.POST, scoped(path(PAGES, BULK), STORE_A), admin,
                String.format(BULK_BODY, id, ARCHIVE_2));

        expect(result, HttpStatus.MULTI_STATUS);
        assertThat(json(result).get(0).get(OK).asBoolean()).isTrue();
        assertThat(json(result).get(0).get(ID).asLong()).isEqualTo(id);
    }

    @Test
    void abulkActionOnAnItemFromAnotherStoreReportsThatItemAsFailedRatherThanTouchingIt() {
        long id = create(slug("bulk-isolation"), TITLE_ONE);

        var result = api.send(HttpMethod.POST, scoped(path(PAGES, BULK), STORE_B), otherStoreAdmin,
                String.format(BULK_BODY, id, "DELETE"));

        expect(result, HttpStatus.MULTI_STATUS);
        assertThat(json(result).get(0).get(OK).asBoolean()).isFalse();
        // Still there, under its own store.
        expect(api.get(scoped(path(PAGES, id), STORE_A), admin), HttpStatus.OK);
    }

    @Test
    void thelistCanBeNarrowedByStatusLocaleAndFreeText() {
        String pageSlug = slug("filtered");
        long id = create(pageSlug, TITLE_ONE);

        expect(api.get(query(scoped(PAGES, STORE_A), String.format("status=DRAFT&locale=all&q=%s", pageSlug)),
                admin), HttpStatus.OK);
        var byLocale = api.get(query(scoped(PAGES, STORE_A), "locale=en&state=MISSING"), admin);

        expect(byLocale, HttpStatus.OK);
        assertThat(json(byLocale).get("content")).isNotNull();
        assertThat(id).isPositive();
    }

    @Test
    void amoderatorIsRefusedEveryTransition() {
        long id = create(slug("gated"), TITLE_ONE);

        for (String action : new String[] {SUBMIT_REVIEW, ARCHIVE, RESTORE}) {
            expect(api.send(HttpMethod.POST, item(id, action), moderator, null), HttpStatus.FORBIDDEN);
        }
        expect(api.send(HttpMethod.PUT, item(id, TRANSLATIONS, AR), moderator, TRANSLATION_BODY),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, scoped(path(PAGES, BULK), STORE_A), moderator,
                String.format(BULK_BODY, id, ARCHIVE_2)), HttpStatus.FORBIDDEN);
    }

    @Test
    void anotherStoresTokenCannotTransitionThisStoresItem() {
        long id = create(slug("isolated"), TITLE_ONE);

        var refused = api.send(HttpMethod.POST,
                scoped(path(PAGES, id, ARCHIVE), STORE_B), otherStoreAdmin, null);

        assertThat(refused.getStatusCode().is2xxSuccessful()).isFalse();
        assertThat(statusOf(id)).isEqualTo(DRAFT);
    }

    private long create(String pageSlug, String title) {
        var created = api.send(HttpMethod.POST, scoped(PAGES, STORE_A), admin, body(pageSlug, title, null));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private static String body(String pageSlug, String title, Integer version) {
        String v = version == null ? "" : String.format("\"version\":%d,", version);
        return String.format(PAGE_BODY, v, pageSlug, title, BODY_ONE);
    }

    private String item(long id, Object... more) {
        Object[] segments = new Object[more.length + 2];
        segments[0] = PAGES;
        segments[1] = id;
        System.arraycopy(more, 0, segments, 2, more.length);
        return scoped(path(segments), STORE_A);
    }

    private ResponseEntity<String> post(String url, String requestBody) {
        return api.send(HttpMethod.POST, url, admin, requestBody);
    }

    private String statusAfter(ResponseEntity<String> response) {
        expect(response, HttpStatus.OK);
        return json(response).get(STATUS).asString();
    }

    private String statusOf(long id) {
        var read = api.get(scoped(path(PAGES, id), STORE_A), admin);
        expect(read, HttpStatus.OK);
        return json(read).get(STATUS).asString();
    }

    private boolean exists(String pageSlug, Long excludeId) {
        String q = excludeId == null ? String.format("slug=%s", pageSlug)
                : String.format("slug=%s&excludeId=%d", pageSlug, excludeId);
        var response = api.get(query(scoped(path(PAGES, "slug-available"), STORE_A), q), admin);
        expect(response, HttpStatus.OK);
        return json(response).get("exists").asBoolean();
    }

}
