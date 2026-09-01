package com.asrevo.cvhome.content.api.v1;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.asrevo.cvhome.content.service.PublishingService;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.time.MutableClock;
import com.asrevo.cvhome.testsupport.time.TestClockConfiguration;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.BODY;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CODE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CONTENT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.EN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STOREFRONT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PUBLISHED;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STATUS;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.VERSION;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.query;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end over HTTP against Postgres + MinIO: the storefront read surface, the page workflow, tenant
 * isolation, the read/manage permission split and scheduling.
 */
@StorageIntegrationTest
@Import(TestClockConfiguration.class)
// ScheduledPublishJob promotes and ARCHIVES content on a timer, against the same clock these tests move. Left on,
// it fires part-way through a slow run (CI, not a laptop) and archives rows behind the assertions. Promotion here
// is driven explicitly through PublishingService.tick().
@TestPropertySource(properties = {
        "com.asrevo.cvhome.content.scheduler.initial-delay=PT24H",
        "com.asrevo.cvhome.content.scheduler.delay=PT24H"})
class ContentApiIntegrationTest {

    /** Seeded store (languages ar, fr). */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String PAGES_SEGMENT = "pages";

    private static final String PAGES = path(PRIVATE, PAGES_SEGMENT);

    private static final String SUMMARY = path(PRIVATE, "summary");

    private static final String ABOUT_US = "about-us";

    private static final String SITE_SETTINGS = "site-settings";

    private static final String SHOP_TITLE = "Acme Supply Co.";

    private static final String SEO = "seo";

    private static final String META_TITLE = "metaTitle";

    private static final String PUBLISH = "publish";

    private static final String REVISIONS = "revisions";

    private static final String CONTENT_PATH = "/content/%s";

    private static final String STOREFRONT_PAGE = "%s/pages/%s?store=%s&lang=%s";

    private static final String PAGE_BODY = """
            {%s"slug":"%s",
             "translations":[{"language":"en","title":"%s","body":"%s"},
                             {"language":"ar","title":"عنوان","body":""}]}""";

    private static final String BODY_ONE = "<p>Body</p>";

    private static final String TITLE_TWO = "Life cycle 2";

    private static final String BODY_TWO = "<p>B2</p>";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private MutableClock clock;

    @Autowired
    private PublishingService publishing;

    private ApiTestSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        clock.reset();
        api = new ApiTestSupport(port, signer);
        admin = api.token(ROLE_STORE_ADMIN, STORE_A);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static String pageBody(String slug, String title, String body, Integer version) {
        String v = version == null ? "" : String.format("\"version\":%d,", version);
        return String.format(PAGE_BODY, v, slug, title, body);
    }

    private static String item(String store, long id, String... more) {
        Object[] segments = new Object[more.length + 2];
        segments[0] = PAGES;
        segments[1] = id;
        System.arraycopy(more, 0, segments, 2, more.length);
        return scoped(path(segments), store);
    }

    private ResponseEntity<String> storefront(String slug, String store, String lang) {
        return api.get(String.format(STOREFRONT_PAGE, STOREFRONT, slug, store, lang), null);
    }

    private long create(String store, String token, String body) {
        var created = api.send(HttpMethod.POST, scoped(PAGES, store), token, body);
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private ResponseEntity<String> post(String url, String body) {
        return api.send(HttpMethod.POST, url, admin, body);
    }

    private ResponseEntity<String> put(String url, String body) {
        return api.send(HttpMethod.PUT, url, admin, body);
    }

    // ------------------------------------------------------------------------------------- workflow

    @Test
    void pageLifecycleCreatePublishReadUnpublishDelete() {
        String slug = slug("lifecycle");
        long id = create(STORE_A, admin, pageBody(slug, "Life cycle", BODY_ONE, null));

        // a draft is invisible to the storefront
        expect(storefront(slug, STORE_A, EN), HttpStatus.NOT_FOUND);

        // read back: locales reflect completeness (ar has a title only → DRAFT)
        var read = api.get(item(STORE_A, id), admin);
        expect(read, HttpStatus.OK);
        JsonNode page = json(read);
        assertThat(page.get("slug").asString()).isEqualTo(slug);
        assertThat(page.get(VERSION).asInt()).isZero();
        assertThat(page.get("locales")).hasSize(2);

        // publish → visible on the storefront surface
        var published = post(item(STORE_A, id, PUBLISH), null);
        expect(published, HttpStatus.OK);
        assertThat(json(published).get(STATUS).asString()).isEqualTo(PUBLISHED);
        var live = storefront(slug, STORE_A, EN);
        expect(live, HttpStatus.OK);
        assertThat(json(live).get(BODY).asString()).isEqualTo(BODY_ONE);

        // list filters by status
        var list = api.get(scoped(query(PAGES, "status=PUBLISHED&q=life"), STORE_A), admin);
        assertThat(json(list).get(CONTENT).size()).isGreaterThanOrEqualTo(1);

        // update with a stale version → 409
        var stale = put(item(STORE_A, id), pageBody(slug, TITLE_TWO, BODY_TWO, 0));
        expect(stale, HttpStatus.CONFLICT);
        assertThat(json(stale).get(CODE).asString()).isEqualTo("CONTENT.VERSION.CONFLICT");

        // update with the current version, changing slug → redirect written
        int current = json(api.get(item(STORE_A, id), admin)).get(VERSION).asInt();
        String moved = String.format("%s-moved", slug);
        expect(put(item(STORE_A, id), pageBody(moved, TITLE_TWO, BODY_TWO, current)), HttpStatus.OK);
        var redirects = api.get(scoped(path(PRIVATE, "redirects"), STORE_A), admin);
        assertThat(redirects.getBody()).contains(String.format(CONTENT_PATH, slug))
                .contains(String.format(CONTENT_PATH, moved));

        // revisions exist
        assertThat(json(api.get(item(STORE_A, id, REVISIONS), admin)).size()).isGreaterThanOrEqualTo(2);

        // unpublish → storefront 404 again; delete → gone
        expect(post(item(STORE_A, id, "unpublish"), null), HttpStatus.OK);
        expect(storefront(moved, STORE_A, EN), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, item(STORE_A, id), admin, null), HttpStatus.NO_CONTENT);
        expect(api.get(item(STORE_A, id), admin), HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateSlugAndIncompletePublishAreRefused() {
        var dup = post(scoped(PAGES, STORE_A), pageBody(ABOUT_US, "Dup", "<p>x</p>", null));
        expect(dup, HttpStatus.CONFLICT);
        assertThat(json(dup).get(CODE).asString()).isEqualTo("CONTENT.SLUG.DUPLICATE");

        long id = create(STORE_A, admin, String.format(
                "{\"slug\":\"%s\",\"translations\":[{\"language\":\"en\",\"title\":\"Only a title\"}]}",
                slug("incomplete")));
        var publish = post(item(STORE_A, id, PUBLISH), null);
        expect(publish, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(publish).get(CODE).asString()).isEqualTo("CONTENT.PUBLISH.INCOMPLETE");

        expect(post(scoped(PAGES, STORE_A), pageBody("Not A Slug", "x", "y", null)), HttpStatus.BAD_REQUEST);

        // The refused publish must leave nothing behind: the domain exceptions are checked, so a write
        // transaction without `rollbackFor` used to commit the schedule window it had already applied.
        var reread = json(api.get(item(STORE_A, id), admin));
        assertThat(reread.get(STATUS).asString()).isEqualTo("DRAFT");
        assertThat(reread.get("publishAt").isNull()).isTrue();
    }

    @Test
    void repeatedBodyOnlyEditsKeepMovingTheVersion() {
        // Title and body live on the child row, so a body-only edit leaves `content` itself untouched. Without
        // a forced increment the version would stand still, the revision snapshot would collide on
        // (content_id, version) and the second save would come back 409.
        String slug = slug("body-only");
        long id = create(STORE_A, admin, pageBody(slug, "First", BODY_ONE, null));
        for (int i = 0; i < 3; i++) {
            int version = json(api.get(item(STORE_A, id), admin)).get(VERSION).asInt();
            var saved = put(item(STORE_A, id),
                    pageBody(slug, String.format("Take %d", i), String.format("<p>take %d</p>", i), version));
            expect(saved, HttpStatus.OK);
            assertThat(json(saved).get(VERSION).asInt()).isGreaterThan(version);
        }
        var read = json(api.get(item(STORE_A, id), admin));
        assertThat(read.get("audit").get("updatedAt").isNull()).isFalse();
        assertThat(json(api.get(item(STORE_A, id, REVISIONS), admin)).size()).isEqualTo(4);
    }

    @Test
    void schedulingPublishesWhenTheClockArrives() {
        String slug = slug("sched");
        long id = create(STORE_A, admin, pageBody(slug, "Soon", "<p>soon</p>", null));
        Instant at = clock.instant().plus(Duration.ofHours(1));
        var scheduled = post(item(STORE_A, id, PUBLISH), String.format("{\"publishAt\":\"%s\"}", at));
        expect(scheduled, HttpStatus.OK);
        assertThat(json(scheduled).get(STATUS).asString()).isEqualTo("SCHEDULED");
        expect(storefront(slug, STORE_A, EN), HttpStatus.NOT_FOUND);

        assertThat(publishing.tick()).isZero();
        clock.advance(Duration.ofHours(2));
        assertThat(publishing.tick()).isEqualTo(1);
        assertThat(publishing.tick()).isZero();
        assertThat(json(api.get(item(STORE_A, id), admin)).get(STATUS).asString()).isEqualTo(PUBLISHED);
        expect(storefront(slug, STORE_A, EN), HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchThePage() {
        String slug = slug("isolated");
        long id = create(STORE_A, admin, pageBody(slug, "Mine", "<p>m</p>", null));
        String other = api.token(ROLE_STORE_ADMIN, STORE_B);
        expect(api.get(item(STORE_B, id), other), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, item(STORE_B, id), other, pageBody(slug, "Hack", "<p>h</p>", 0)),
                HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, item(STORE_B, id), other, null), HttpStatus.NOT_FOUND);
        // a store-B token asking for store A is forbidden outright
        expect(api.get(item(STORE_A, id), other), HttpStatus.FORBIDDEN);
        // and the public surface never leaks the other store's slug
        expect(storefront(slug, STORE_B, EN), HttpStatus.NOT_FOUND);
    }

    @Test
    void moderatorCanReadButNotWrite() {
        String mod = api.token(ROLE_STORE_MODERATOR, STORE_A);
        expect(api.get(scoped(PAGES, STORE_A), mod), HttpStatus.OK);
        expect(api.get(scoped(SUMMARY, STORE_A), mod), HttpStatus.OK);
        expect(api.send(HttpMethod.POST, scoped(PAGES, STORE_A), mod, pageBody(slug("mod"), "t", "b", null)),
                HttpStatus.FORBIDDEN);
        expect(api.get(scoped(PAGES, STORE_A), null), HttpStatus.UNAUTHORIZED);
    }

    // ----------------------------------------------------------------------------- summary + site settings

    @Test
    void summaryCountsAndSiteSettingsRoundTrip() {
        JsonNode summary = json(api.get(scoped(SUMMARY, STORE_A), admin));
        JsonNode counts = summary.get("counts");
        assertThat(counts.get(PAGES_SEGMENT).asLong()).isGreaterThanOrEqualTo(6);
        assertThat(counts.has("banners")).isTrue();
        assertThat(summary.get("publishedItems").asLong()).isGreaterThanOrEqualTo(6);
        assertThat(summary.get("media").get("bytesQuota").asLong()).isGreaterThan(0);

        // The store's title and description used to be `meta-title` / `meta-description` BOX rows. The seed
        // carries them across, so they read back before anything is written.
        String settingsUrl = scoped(path(PRIVATE, SITE_SETTINGS), STORE_A);
        assertThat(json(api.get(settingsUrl, admin)).get(SEO).has(META_TITLE)).isTrue();

        var saved = put(settingsUrl, """
                {"logoMediaId":null,"faviconMediaId":null,"ogMediaId":null,
                 "seo":{"metaTitle":{"en":"Acme Supply Co."}},
                 "socialLinks":[{"provider":"INSTAGRAM","url":"https://instagram.com/acme"}]}""");
        expect(saved, HttpStatus.OK);

        JsonNode s = json(api.get(settingsUrl, admin));
        assertThat(s.get(SEO).get(META_TITLE).get("en").asString()).isEqualTo(SHOP_TITLE);
        assertThat(s.get("socialLinks").get(0).get("provider").asString()).isEqualTo("INSTAGRAM");
        assertThat(s.get("branding").get("logo").isNull()).isTrue();
    }

    /**
     * A media id from another store must not be attachable, or one seller could point at another's asset.
     */
    @Test
    void siteSettingsRefuseAMediaIdFromAnotherStore() {
        var refused = put(scoped(path(PRIVATE, SITE_SETTINGS), STORE_A),
                """
                {"logoMediaId":999999,"seo":{},"socialLinks":[]}""");
        expect(refused, HttpStatus.NOT_FOUND);
        assertThat(refused.getBody()).contains("MEDIA.NOT_FOUND");
    }

}
