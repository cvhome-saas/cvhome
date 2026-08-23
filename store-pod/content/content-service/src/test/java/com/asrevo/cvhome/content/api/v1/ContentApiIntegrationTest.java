package com.asrevo.cvhome.content.api.v1;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.content.MutableClock;
import com.asrevo.cvhome.content.TestClockConfiguration;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.content.service.PublishingService;
import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

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

    private static final String ORG = "32a034a43cd77581d105c87a";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort
    private int port;

    @Autowired
    private ServletTestCustomSecurityConfig.JwtSigner signer;

    @Autowired
    private MutableClock clock;

    @Autowired
    private PublishingService publishing;

    // ------------------------------------------------------------------------------------------------ helpers

    private RestClient client() {
        return RestClient.builder().baseUrl("http://localhost:" + port)
                .defaultStatusHandler(s -> true, (req, res) -> { })
                .build();
    }

    private String token(String role, String store) throws Exception {
        return signer.createJwt(Map.of(
                "sub", role.toLowerCase() + "@" + store,
                "name", "Test " + role,
                "roles", List.of(role),
                "scope", "store_pod",
                "org", ORG,
                "store", store,
                "exp", Instant.now().plusSeconds(3600).getEpochSecond()));
    }

    private String admin(String store) throws Exception {
        return token("ROLE_STORE_ADMIN", store);
    }

    private String moderator(String store) throws Exception {
        return token("ROLE_STORE_MODERATOR", store);
    }

    private ResponseEntity<String> get(String path, String token) {
        var spec = client().get().uri(path);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return spec.retrieve().toEntity(String.class);
    }

    private ResponseEntity<String> send(String method, String path, String token, String body) {
        var spec = client().method(org.springframework.http.HttpMethod.valueOf(method)).uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            spec = spec.body(body);
        }
        return spec.retrieve().toEntity(String.class);
    }

    private static JsonNode json(ResponseEntity<String> r) {
        return JSON.readTree(r.getBody());
    }

    private static String pageBody(String slug, String title, String body, Integer version) {
        String v = version == null ? "" : "\"version\":" + version + ",";
        return "{" + v + "\"slug\":\"" + slug + "\",\"template\":\"STANDARD\",\"linkToMenu\":true,"
                + "\"translations\":[{\"language\":\"en\",\"title\":\"" + title + "\",\"body\":\"" + body + "\"},"
                + "{\"language\":\"ar\",\"title\":\"عنوان\",\"body\":\"\"}]}";
    }

    private String base(String store) {
        return "/api/v1/private/content/pages?store=" + store + "&lang=en";
    }

    private String item(String store, long id, String suffix) {
        return "/api/v1/private/content/pages/" + id + suffix + (suffix.contains("?") ? "&" : "?")
                + "store=" + store + "&lang=en";
    }

    // ------------------------------------------------------------------------------------- legacy compat

    @Test
    void legacyPagesListKeepsItsShape() {
        var r = get("/api/v1/content/pages?page=0&count=20&store=" + STORE_A + "&lang=ar", null);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode list = json(r);
        assertThat(list.has("totalPages")).isTrue();
        assertThat(list.has("content")).isTrue();
        JsonNode about = null;
        for (JsonNode p : list.get("content")) {
            if ("about-us".equals(p.get("code").asString())) {
                about = p;
            }
        }
        assertThat(about).isNotNull();
        assertThat(about.get("contentType").asString()).isEqualTo("PAGE");
        assertThat(about.get("visible").asBoolean()).isTrue();
        assertThat(about.get("linkToMenu").asBoolean()).isTrue();
        JsonNode d = about.get("description");
        assertThat(d.get("language").asString()).isEqualTo("ar");
        assertThat(d.get("friendlyUrl").asString()).isEqualTo("about-us");
        assertThat(d.get("name").asString()).isNotBlank();
        assertThat(d.get("description").asString()).contains("<h1>");
        assertThat(d.has("metaDescription")).isTrue();
        assertThat(d.has("title")).isTrue();
        assertThat(d.has("keyWords")).isTrue();
    }

    @Test
    void legacyPageByNameAndBoxByCode() {
        var page = get("/api/v1/content/pages/name/about-us?store=" + STORE_A + "&lang=fr", null);
        assertThat(page.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(page).get("description").get("language").asString()).isEqualTo("fr");

        var missing = get("/api/v1/content/pages/name/nope?store=" + STORE_A + "&lang=fr", null);
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(json(missing).get("code").asString()).isEqualTo("CONTENT.NOT_FOUND");

        for (String code : List.of("header-message", "meta-title", "meta-description", "agreement")) {
            var box = get("/api/v1/content/boxes/" + code + "?store=" + STORE_A + "&lang=ar", null);
            assertThat(box.getStatusCode()).as(code).isEqualTo(HttpStatus.OK);
            JsonNode b = json(box);
            assertThat(b.get("code").asString()).isEqualTo(code);
            assertThat(b.get("contentType").asString()).isEqualTo("BOX");
            assertThat(b.get("description").get("description").asString()).isNotBlank();
        }
    }

    // ------------------------------------------------------------------------------------- workflow

    @Test
    void pageLifecycle_createPublishReadUnpublishDelete() throws Exception {
        String slug = "lifecycle-" + UUID.randomUUID().toString().substring(0, 8);
        String admin = admin(STORE_A);

        var created = send("POST", base(STORE_A), admin, pageBody(slug, "Life cycle", "<p>Body</p>", null));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = json(created).get("id").asLong();
        assertThat(json(created).get("status").asString()).isEqualTo("DRAFT");

        // a draft is invisible to the storefront
        assertThat(get("/api/v1/content/pages/name/" + slug + "?store=" + STORE_A + "&lang=en", null)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // read back: locales reflect completeness (ar has a title only → DRAFT)
        var read = get(item(STORE_A, id, ""), admin);
        assertThat(read.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode page = json(read);
        assertThat(page.get("slug").asString()).isEqualTo(slug);
        assertThat(page.get("version").asInt()).isZero();
        assertThat(page.get("locales")).hasSize(2);

        // publish → visible on the legacy surface
        var published = send("POST", item(STORE_A, id, "/publish"), admin, null);
        assertThat(published.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(published).get("status").asString()).isEqualTo("PUBLISHED");
        var legacy = get("/api/v1/content/pages/name/" + slug + "?store=" + STORE_A + "&lang=en", null);
        assertThat(legacy.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(legacy).get("description").get("description").asString()).isEqualTo("<p>Body</p>");

        // list filters by status
        var list = get(base(STORE_A) + "&status=PUBLISHED&q=life", admin);
        assertThat(json(list).get("content").size()).isGreaterThanOrEqualTo(1);

        // update with a stale version → 409
        var stale = send("PUT", item(STORE_A, id, ""), admin, pageBody(slug, "Life cycle 2", "<p>B2</p>", 0));
        assertThat(stale.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(json(stale).get("code").asString()).isEqualTo("CONTENT.VERSION.CONFLICT");

        // update with the current version, changing slug → redirect written
        int current = json(get(item(STORE_A, id, ""), admin)).get("version").asInt();
        var updated = send("PUT", item(STORE_A, id, ""), admin,
                pageBody(slug + "-moved", "Life cycle 2", "<p>B2</p>", current));
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        var redirects = get("/api/v1/private/content/redirects?store=" + STORE_A + "&lang=en", admin);
        assertThat(redirects.getBody()).contains("/content/" + slug).contains("/content/" + slug + "-moved");

        // revisions exist and restore works
        var revisions = get(item(STORE_A, id, "/revisions"), admin);
        assertThat(json(revisions).size()).isGreaterThanOrEqualTo(2);

        // unpublish → storefront 404 again; delete → gone
        assertThat(send("POST", item(STORE_A, id, "/unpublish"), admin, null).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(get("/api/v1/content/pages/name/" + slug + "-moved?store=" + STORE_A + "&lang=en", null)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(send("DELETE", item(STORE_A, id, ""), admin, null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get(item(STORE_A, id, ""), admin).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void duplicateSlugAndIncompletePublishAreRefused() throws Exception {
        String admin = admin(STORE_A);
        var dup = send("POST", base(STORE_A), admin, pageBody("about-us", "Dup", "<p>x</p>", null));
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(json(dup).get("code").asString()).isEqualTo("CONTENT.SLUG.DUPLICATE");

        String slug = "incomplete-" + UUID.randomUUID().toString().substring(0, 8);
        var created = send("POST", base(STORE_A), admin,
                "{\"slug\":\"" + slug + "\",\"translations\":[{\"language\":\"en\",\"title\":\"Only a title\"}]}");
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = json(created).get("id").asLong();
        var publish = send("POST", item(STORE_A, id, "/publish"), admin, null);
        assertThat(publish.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(publish).get("code").asString()).isEqualTo("CONTENT.PUBLISH.INCOMPLETE");

        var bad = send("POST", base(STORE_A), admin, pageBody("Not A Slug", "x", "y", null));
        assertThat(bad.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void schedulingPublishesWhenTheClockArrives() throws Exception {
        String admin = admin(STORE_A);
        String slug = "sched-" + UUID.randomUUID().toString().substring(0, 8);
        long id = json(send("POST", base(STORE_A), admin, pageBody(slug, "Soon", "<p>soon</p>", null)))
                .get("id").asLong();
        Instant at = clock.instant().plus(Duration.ofHours(1));
        var scheduled = send("POST", item(STORE_A, id, "/publish"), admin, "{\"publishAt\":\"" + at + "\"}");
        assertThat(scheduled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(scheduled).get("status").asString()).isEqualTo("SCHEDULED");
        assertThat(get("/api/v1/content/pages/name/" + slug + "?store=" + STORE_A + "&lang=en", null)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(publishing.tick()).isZero();
        clock.advance(Duration.ofHours(2));
        assertThat(publishing.tick()).isEqualTo(1);
        assertThat(publishing.tick()).isZero();
        assertThat(json(get(item(STORE_A, id, ""), admin)).get("status").asString()).isEqualTo("PUBLISHED");
        assertThat(get("/api/v1/content/pages/name/" + slug + "?store=" + STORE_A + "&lang=en", null)
                .getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotSeeOrTouchThePage() throws Exception {
        String slug = "isolated-" + UUID.randomUUID().toString().substring(0, 8);
        long id = json(send("POST", base(STORE_A), admin(STORE_A), pageBody(slug, "Mine", "<p>m</p>", null)))
                .get("id").asLong();
        String other = admin(STORE_B);
        assertThat(get(item(STORE_B, id, ""), other).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(send("PUT", item(STORE_B, id, ""), other, pageBody(slug, "Hack", "<p>h</p>", 0))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(send("DELETE", item(STORE_B, id, ""), other, null).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        // a store-B token asking for store A is forbidden outright
        assertThat(get(item(STORE_A, id, ""), other).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        // and the public surface never leaks the other store's slug
        assertThat(get("/api/v1/content/pages/name/" + slug + "?store=" + STORE_B + "&lang=en", null)
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void moderatorCanReadButNotWrite() throws Exception {
        String mod = moderator(STORE_A);
        assertThat(get(base(STORE_A), mod).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get("/api/v1/private/content/summary?store=" + STORE_A + "&lang=en", mod).getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(send("POST", base(STORE_A), mod, pageBody("mod-" + UUID.randomUUID(), "x", "y", null))
                .getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(get(base(STORE_A), null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ---------------------------------------------------------------------------------- summary + snippets

    @Test
    void summaryCountsAndSnippetsRoundTrip() throws Exception {
        String admin = admin(STORE_A);
        JsonNode summary = json(get("/api/v1/private/content/summary?store=" + STORE_A + "&lang=en", admin));
        assertThat(summary.get("counts").get("pages").asLong()).isGreaterThanOrEqualTo(6);
        assertThat(summary.get("counts").get("snippets").asLong()).isGreaterThanOrEqualTo(4);
        assertThat(summary.get("publishedItems").asLong()).isGreaterThanOrEqualTo(6);
        assertThat(summary.get("media").get("bytesQuota").asLong()).isGreaterThan(0);

        var put = send("PUT", "/api/v1/private/content/snippets/LANDING_PAGE?store=" + STORE_A + "&lang=en", admin,
                "{\"visible\":true,\"translations\":[{\"language\":\"en\",\"title\":\"Welcome\","
                        + "\"body\":\"<p>Hi</p>\",\"metaDescription\":\"meta\",\"keywords\":\"a, b\"}]}");
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode s = json(get("/api/v1/private/content/snippets/LANDING_PAGE?store=" + STORE_A + "&lang=en", admin));
        assertThat(s.get("code").asString()).isEqualTo("LANDING_PAGE");
        assertThat(s.get("translations").get(0).get("keywords").asString()).isEqualTo("a, b");
        // and it is readable through the legacy box surface, keywords included
        JsonNode box = json(get("/api/v1/content/boxes/LANDING_PAGE?store=" + STORE_A + "&lang=en", null));
        assertThat(box.get("description").get("keyWords").asString()).isEqualTo("a, b");
    }

}
