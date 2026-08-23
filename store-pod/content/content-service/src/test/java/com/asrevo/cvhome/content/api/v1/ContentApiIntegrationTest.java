package com.asrevo.cvhome.content.api.v1;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.asrevo.cvhome.content.MutableClock;
import com.asrevo.cvhome.content.TestClockConfiguration;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.service.PublishingService;
import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CODE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CONTENT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.DESCRIPTION;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.EN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.LEGACY;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
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
 * End-to-end over HTTP against Postgres + MinIO: the legacy compat shapes the storefront depends on, the page
 * workflow, tenant isolation, the read/manage permission split and scheduling.
 */
@Import({TestcontainersConfiguration.class, TestClockConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"signer", "test-stores"})
@Tag("integration-test")
class ContentApiIntegrationTest {

    /** Seeded store (languages ar, fr). */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store. */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String PAGES_SEGMENT = "pages";

    private static final String PAGES = path(PRIVATE, PAGES_SEGMENT);

    private static final String SUMMARY = path(PRIVATE, "summary");

    private static final String ABOUT_US = "about-us";

    private static final String FR = "fr";

    private static final String AR = "ar";

    private static final String KEY_WORDS = "keyWords";

    private static final String SNIPPETS = "snippets";

    private static final String LANDING_PAGE = "LANDING_PAGE";

    private static final String LANGUAGE = "language";

    private static final String CONTENT_TYPE = "contentType";

    private static final String KEYWORDS = "a, b";

    private static final String PUBLISH = "publish";

    private static final String CONTENT_PATH = "/content/%s";

    private static final String LEGACY_PAGE_BY_NAME = "%s/pages/name/%s?store=%s&lang=%s";

    private static final String LEGACY_BOX = "%s/boxes/%s?store=%s&lang=%s";

    private static final String PAGE_BODY = """
            {%s"slug":"%s","template":"STANDARD","linkToMenu":true,
             "translations":[{"language":"en","title":"%s","body":"%s"},
                             {"language":"ar","title":"عنوان","body":""}]}""";

    private static final String BODY_ONE = "<p>Body</p>";

    private static final String TITLE_TWO = "Life cycle 2";

    private static final String BODY_TWO = "<p>B2</p>";

    @LocalServerPort
    private int port;

    @Autowired
    private ServletTestCustomSecurityConfig.JwtSigner signer;

    @Autowired
    private MutableClock clock;

    @Autowired
    private PublishingService publishing;

    private ApiTestSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
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

    private ResponseEntity<String> legacy(String slug, String store, String lang) {
        return api.get(String.format(LEGACY_PAGE_BY_NAME, LEGACY, slug, store, lang), null);
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

    // ------------------------------------------------------------------------------------- legacy compat

    @Test
    void legacyPagesListKeepsItsShape() {
        var r = api.get(String.format("%s/pages?page=0&count=20&store=%s&lang=ar", LEGACY, STORE_A), null);
        expect(r, HttpStatus.OK);
        JsonNode list = json(r);
        assertThat(list.has("totalPages")).isTrue();
        assertThat(list.has(CONTENT)).isTrue();
        JsonNode about = null;
        for (JsonNode p : list.get(CONTENT)) {
            if (ABOUT_US.equals(p.get(CODE).asString())) {
                about = p;
            }
        }
        assertThat(about).isNotNull();
        assertThat(about.get(CONTENT_TYPE).asString()).isEqualTo("PAGE");
        assertThat(about.get("visible").asBoolean()).isTrue();
        assertThat(about.get("linkToMenu").asBoolean()).isTrue();
        JsonNode d = about.get(DESCRIPTION);
        assertThat(d.get(LANGUAGE).asString()).isEqualTo(AR);
        assertThat(d.get("friendlyUrl").asString()).isEqualTo(ABOUT_US);
        assertThat(d.get("name").asString()).isNotBlank();
        assertThat(d.get(DESCRIPTION).asString()).contains("<h1>");
        assertThat(d.has("metaDescription")).isTrue();
        assertThat(d.has("title")).isTrue();
        assertThat(d.has(KEY_WORDS)).isTrue();
    }

    @Test
    void legacyPageByNameAndBoxByCode() {
        var page = legacy(ABOUT_US, STORE_A, FR);
        expect(page, HttpStatus.OK);
        assertThat(json(page).get(DESCRIPTION).get(LANGUAGE).asString()).isEqualTo(FR);

        var missing = legacy("nope", STORE_A, FR);
        expect(missing, HttpStatus.NOT_FOUND);
        assertThat(json(missing).get(CODE).asString()).isEqualTo("CONTENT.NOT_FOUND");

        for (String code : List.of("header-message", "meta-title", "meta-description", "agreement")) {
            var box = api.get(String.format(LEGACY_BOX, LEGACY, code, STORE_A, AR), null);
            expect(box, HttpStatus.OK);
            JsonNode b = json(box);
            assertThat(b.get(CODE).asString()).isEqualTo(code);
            assertThat(b.get(CONTENT_TYPE).asString()).isEqualTo("BOX");
            assertThat(b.get(DESCRIPTION).get(DESCRIPTION).asString()).isNotBlank();
        }
    }

    // ------------------------------------------------------------------------------------- workflow

    @Test
    void pageLifecycleCreatePublishReadUnpublishDelete() {
        String slug = slug("lifecycle");
        long id = create(STORE_A, admin, pageBody(slug, "Life cycle", BODY_ONE, null));

        // a draft is invisible to the storefront
        expect(legacy(slug, STORE_A, EN), HttpStatus.NOT_FOUND);

        // read back: locales reflect completeness (ar has a title only → DRAFT)
        var read = api.get(item(STORE_A, id), admin);
        expect(read, HttpStatus.OK);
        JsonNode page = json(read);
        assertThat(page.get("slug").asString()).isEqualTo(slug);
        assertThat(page.get(VERSION).asInt()).isZero();
        assertThat(page.get("locales")).hasSize(2);

        // publish → visible on the legacy surface
        var published = post(item(STORE_A, id, PUBLISH), null);
        expect(published, HttpStatus.OK);
        assertThat(json(published).get(STATUS).asString()).isEqualTo(PUBLISHED);
        var live = legacy(slug, STORE_A, EN);
        expect(live, HttpStatus.OK);
        assertThat(json(live).get(DESCRIPTION).get(DESCRIPTION).asString()).isEqualTo(BODY_ONE);

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
        assertThat(json(api.get(item(STORE_A, id, "revisions"), admin)).size()).isGreaterThanOrEqualTo(2);

        // unpublish → storefront 404 again; delete → gone
        expect(post(item(STORE_A, id, "unpublish"), null), HttpStatus.OK);
        expect(legacy(moved, STORE_A, EN), HttpStatus.NOT_FOUND);
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
    }

    @Test
    void schedulingPublishesWhenTheClockArrives() {
        String slug = slug("sched");
        long id = create(STORE_A, admin, pageBody(slug, "Soon", "<p>soon</p>", null));
        Instant at = clock.instant().plus(Duration.ofHours(1));
        var scheduled = post(item(STORE_A, id, PUBLISH), String.format("{\"publishAt\":\"%s\"}", at));
        expect(scheduled, HttpStatus.OK);
        assertThat(json(scheduled).get(STATUS).asString()).isEqualTo("SCHEDULED");
        expect(legacy(slug, STORE_A, EN), HttpStatus.NOT_FOUND);

        assertThat(publishing.tick()).isZero();
        clock.advance(Duration.ofHours(2));
        assertThat(publishing.tick()).isEqualTo(1);
        assertThat(publishing.tick()).isZero();
        assertThat(json(api.get(item(STORE_A, id), admin)).get(STATUS).asString()).isEqualTo(PUBLISHED);
        expect(legacy(slug, STORE_A, EN), HttpStatus.OK);
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
        expect(legacy(slug, STORE_B, EN), HttpStatus.NOT_FOUND);
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

    // ---------------------------------------------------------------------------------- summary + snippets

    @Test
    void summaryCountsAndSnippetsRoundTrip() {
        JsonNode summary = json(api.get(scoped(SUMMARY, STORE_A), admin));
        JsonNode counts = summary.get("counts");
        assertThat(counts.get(PAGES_SEGMENT).asLong()).isGreaterThanOrEqualTo(6);
        assertThat(counts.get(SNIPPETS).asLong()).isGreaterThanOrEqualTo(4);
        assertThat(summary.get("publishedItems").asLong()).isGreaterThanOrEqualTo(6);
        assertThat(summary.get("media").get("bytesQuota").asLong()).isGreaterThan(0);

        String snippetUrl = scoped(path(PRIVATE, SNIPPETS, LANDING_PAGE), STORE_A);
        var saved = put(snippetUrl, """
                {"visible":true,"translations":[{"language":"en","title":"Welcome","body":"<p>Hi</p>",
                 "metaDescription":"meta","keywords":"a, b"}]}""");
        expect(saved, HttpStatus.OK);
        JsonNode s = json(api.get(snippetUrl, admin));
        assertThat(s.get(CODE).asString()).isEqualTo(LANDING_PAGE);
        assertThat(s.get("translations").get(0).get("keywords").asString()).isEqualTo(KEYWORDS);
        // and it is readable through the legacy box surface, keywords included
        JsonNode box = json(api.get(String.format(LEGACY_BOX, LEGACY, LANDING_PAGE, STORE_A, EN), null));
        assertThat(box.get(DESCRIPTION).get(KEY_WORDS).asString()).isEqualTo(KEYWORDS);
    }

}
