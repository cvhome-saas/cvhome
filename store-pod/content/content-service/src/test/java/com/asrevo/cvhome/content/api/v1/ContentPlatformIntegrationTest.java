package com.asrevo.cvhome.content.api.v1;

import java.nio.charset.StandardCharsets;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.content.TestClockConfiguration;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The phase-2 domains end to end: media (dedupe, quota/type rules, delete guard), banners (capacity, effective),
 * FAQ (groups, reorder, storefront + JSON-LD), policies (version cut on publish, storefront read), posts, menus
 * and the aggregated storefront site document.
 */
@Import({TestcontainersConfiguration.class, TestClockConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"signer", "test-stores"})
@Tag("integration-test")
class ContentPlatformIntegrationTest {

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String ORG = "32a034a43cd77581d105c87a";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String P = "/api/v1/private/content";

    private static final String SF = "/api/v1/storefront";

    // 1x1 PNG
    private static final byte[] PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    @LocalServerPort
    private int port;

    @Autowired
    private ServletTestCustomSecurityConfig.JwtSigner signer;

    private String admin;

    // ---------------------------------------------------------------------------------------------- helpers

    private String admin() throws Exception {
        if (admin == null) {
            admin = signer.createJwt(Map.of("sub", "admin@" + STORE, "name", "Ada Admin",
                    "roles", List.of("ROLE_STORE_ADMIN"), "scope", "store_pod", "org", ORG, "store", STORE,
                    "exp", Instant.now().plusSeconds(3600).getEpochSecond()));
        }
        return admin;
    }

    private RestClient client() {
        return RestClient.builder().baseUrl("http://localhost:" + port)
                .defaultStatusHandler(s -> true, (req, res) -> { }).build();
    }

    private String q(String path) {
        return path + (path.contains("?") ? "&" : "?") + "store=" + STORE + "&lang=en";
    }

    private ResponseEntity<String> get(String path, boolean auth) throws Exception {
        var spec = client().get().uri(q(path));
        if (auth) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + admin());
        }
        return spec.retrieve().toEntity(String.class);
    }

    private ResponseEntity<String> send(String method, String path, String body) throws Exception {
        var spec = client().method(HttpMethod.valueOf(method)).uri(q(path))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + admin())
                .contentType(MediaType.APPLICATION_JSON);
        if (body != null) {
            spec = spec.body(body);
        }
        return spec.retrieve().toEntity(String.class);
    }

    private ResponseEntity<String> upload(String filename, String type, byte[] bytes) throws Exception {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("files", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return client().post().uri(q(P + "/media")).header(HttpHeaders.AUTHORIZATION, "Bearer " + admin())
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form).retrieve().toEntity(String.class);
    }

    private static JsonNode json(ResponseEntity<String> r) {
        return JSON.readTree(r.getBody());
    }

    private static String slug(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private long createAndPublish(String type, String body) throws Exception {
        var created = send("POST", P + "/" + type, body);
        assertThat(created.getStatusCode()).as(created.getBody()).isEqualTo(HttpStatus.CREATED);
        long id = json(created).get("id").asLong();
        var published = send("POST", P + "/" + type + "/" + id + "/publish", null);
        assertThat(published.getStatusCode()).as(published.getBody()).isEqualTo(HttpStatus.OK);
        return id;
    }

    // ------------------------------------------------------------------------------------------------ media

    @Test
    void mediaUploadDedupesAndGuardsDelete() throws Exception {
        var first = upload("pixel.png", "image/png", PNG);
        assertThat(first.getStatusCode()).as(first.getBody()).isEqualTo(HttpStatus.CREATED);
        JsonNode asset = json(first).get(0);
        long id = asset.get("id").asLong();
        assertThat(asset.get("kind").asString()).isEqualTo("IMAGE");
        assertThat(asset.get("width").asInt()).isEqualTo(1);
        assertThat(asset.get("url").asString()).contains("/files/" + STORE + "/media/" + id + "/pixel.png");

        // same bytes again → the same asset, nothing stored twice
        var again = upload("copy.png", "image/png", PNG);
        assertThat(json(again).get(0).get("id").asLong()).isEqualTo(id);

        // a type we do not accept
        var exe = upload("x.exe", "application/octet-stream", "MZ".getBytes(StandardCharsets.UTF_8));
        assertThat(exe.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(exe).get("code").asString()).isEqualTo("MEDIA.TYPE_NOT_ALLOWED");

        // svg is sanitised
        var svg = upload("logo.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script><rect onclick=\"x()\"/></svg>"
                        .getBytes(StandardCharsets.UTF_8));
        assertThat(svg.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(json(svg).get(0).get("kind").asString()).isEqualTo("VECTOR");

        // folders exist by default, list filters
        JsonNode folders = json(get(P + "/media/folders", true));
        assertThat(folders.size()).isGreaterThanOrEqualTo(5);
        JsonNode list = json(get(P + "/media?kind=IMAGE", true));
        assertThat(list.get("content").size()).isGreaterThanOrEqualTo(1);

        // reference it from a post, then deleting is a 409 until forced
        String postSlug = slug("post");
        long postId = createAndPublish("posts", "{\"slug\":\"" + postSlug + "\",\"heroMediaId\":" + id
                + ",\"tags\":[\"news\"],\"translations\":[{\"language\":\"en\",\"title\":\"Hello\","
                + "\"body\":\"<p>" + "word ".repeat(450) + "</p>\"}]}");
        JsonNode usage = json(get(P + "/media/" + id + "/usage", true));
        assertThat(usage.size()).isEqualTo(1);
        assertThat(usage.get(0).get("itemId").asLong()).isEqualTo(postId);
        var del = send("DELETE", P + "/media/" + id, null);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(json(del).get("code").asString()).isEqualTo("MEDIA.REFERENCED");

        // the storefront post carries the hero url and reading minutes
        JsonNode sfPost = json(get(SF + "/posts/" + postSlug, false));
        assertThat(sfPost.get("heroImageUrl").asString()).contains("pixel.png");
        assertThat(sfPost.get("readingMinutes").asInt()).isEqualTo(3);
        assertThat(sfPost.get("tags").get(0).asString()).isEqualTo("news");
        JsonNode sfList = json(get(SF + "/posts?tag=news", false));
        assertThat(sfList.get("content").size()).isGreaterThanOrEqualTo(1);

        assertThat(send("DELETE", P + "/media/" + id + "?force=true", null).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(get(P + "/media/" + id, true).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode summary = json(get(P + "/summary", true));
        assertThat(summary.get("media").get("fileCount").asLong()).isGreaterThanOrEqualTo(1);
    }

    // ---------------------------------------------------------------------------------------------- banners

    @Test
    void heroPlacementHoldsOneBannerAndStripFeedsTheAnnouncement() throws Exception {
        String banner = "{\"slug\":\"%s\",\"placement\":\"%s\",\"target\":{\"kind\":\"URL\",\"value\":\"/x\"},"
                + "\"translations\":[{\"language\":\"en\",\"title\":\"%s\",\"subtitle\":\"sub\",\"ctaLabel\":\"Go\"}]}";
        long hero1 = createAndPublish("banners", String.format(banner, slug("hero"), "HERO", "Hero one"));
        var second = send("POST", P + "/banners", String.format(banner, slug("hero"), "HERO", "Hero two"));
        long hero2 = json(second).get("id").asLong();
        var publish2 = send("POST", P + "/banners/" + hero2 + "/publish", null);
        assertThat(publish2.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(publish2.getBody()).contains("CONTENT.BANNER.CAPACITY_EXCEEDED");

        JsonNode effective = json(get(P + "/banners/effective?placement=HERO", true));
        assertThat(effective.size()).isEqualTo(1);
        assertThat(effective.get(0).get("id").asLong()).isEqualTo(hero1);

        createAndPublish("banners", String.format(banner, slug("strip"), "STRIP", "Free delivery over 250"));
        JsonNode site = json(get(SF + "/site", false));
        assertThat(site.get("announcement").get("title").asString()).isEqualTo("Free delivery over 250");
        assertThat(site.get("menus").has("main")).isTrue();
        assertThat(site.get("footerPages").size()).isGreaterThanOrEqualTo(1);
        JsonNode sfBanners = json(get(SF + "/banners?placement=HERO", false));
        assertThat(sfBanners.get(0).get("ctaLabel").asString()).isEqualTo("Go");

        // the console row subtitle names the placement and target
        JsonNode rows = json(get(P + "/banners?status=PUBLISHED", true));
        assertThat(rows.get("content").toString()).contains("hero · /x");
    }

    // -------------------------------------------------------------------------------------------------- faq

    @Test
    void faqGroupsReorderAndRenderWithJsonLd() throws Exception {
        JsonNode groups = json(get(P + "/faq/groups", true));
        assertThat(groups.size()).isGreaterThanOrEqualTo(4);
        long shipping = -1;
        long ordering = -1;
        for (JsonNode g : groups) {
            if ("shipping".equals(g.get("key").asString())) {
                shipping = g.get("id").asLong();
            }
            if ("ordering".equals(g.get("key").asString())) {
                ordering = g.get("id").asLong();
            }
        }
        String entry = "{\"slug\":\"%s\",\"groupId\":%d,\"keywords\":[\"delivery\"],\"showInCheckoutHelp\":true,"
                + "\"translations\":[{\"language\":\"en\",\"title\":\"%s\",\"body\":\"<p>%s</p>\"}]}";
        long a = createAndPublish("faq", String.format(entry, slug("faq"), shipping, "How fast?", "Next day."));
        long b = createAndPublish("faq", String.format(entry, slug("faq"), shipping, "Weekends?", "Yes."));

        // move b to the front of ordering, a stays in shipping
        var reorder = send("PATCH", P + "/faq/reorder",
                "[{\"id\":" + b + ",\"groupId\":" + ordering + ",\"position\":0}]");
        assertThat(reorder.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        JsonNode readB = json(get(P + "/faq/" + b, true));
        assertThat(readB.get("groupId").asLong()).isEqualTo(ordering);
        assertThat(readB.get("position").asInt()).isZero();
        assertThat(readB.get("groupName").asString()).isEqualTo("Ordering");

        JsonNode sf = json(get(SF + "/faq", false));
        assertThat(sf.get("jsonLd").asString()).contains("\"@type\":\"FAQPage\"").contains("How fast?");
        boolean found = false;
        for (JsonNode g : sf.get("groups")) {
            for (JsonNode e : g.get("entries")) {
                if (e.get("id").asLong() == a) {
                    found = "shipping".equals(g.get("key").asString());
                }
            }
        }
        assertThat(found).isTrue();
        JsonNode rows = json(get(P + "/faq", true));
        assertThat(rows.get("content").toString()).contains("position 1");
    }

    // ---------------------------------------------------------------------------------------------- policies

    @Test
    void publishingAPolicyCutsVersionsTheStorefrontServes() throws Exception {
        String slug = slug("returns");
        String body = "{\"slug\":\"%s\",\"policyType\":\"RETURNS\",\"jurisdiction\":\"NL\","
                + "\"requiresAcceptance\":true,\"translations\":[{\"language\":\"en\",\"title\":\"Returns\","
                + "\"body\":\"<p>%s</p>\"}]}";
        // a template exists
        JsonNode template = json(get(P + "/policies/templates?type=RETURNS&jurisdiction=NL", true));
        assertThat(template.get("translations").size()).isEqualTo(2);

        long id = createAndPublish("policies", String.format(body, slug, "30 days"));
        JsonNode read = json(get(P + "/policies/" + id, true));
        assertThat(read.get("liveVersion").asInt()).isEqualTo(1);
        assertThat(read.get("versions").size()).isEqualTo(1);

        // a second head of the same type is refused
        var dup = send("POST", P + "/policies", String.format(body, slug("returns"), "x"));
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(json(dup).get("code").asString()).isEqualTo("CONTENT.POLICY.TYPE_ACTIVE_EXISTS");

        // edit the text and publish again → version 2 live, version 1 archived, both readable
        int version = read.get("version").asInt();
        var updated = send("PUT", P + "/policies/" + id,
                String.format(body, slug, "14 days").replace("{\"slug\"", "{\"version\":" + version + ",\"slug\""));
        assertThat(updated.getStatusCode()).as(updated.getBody()).isEqualTo(HttpStatus.OK);
        var republished = send("POST", P + "/policies/" + id + "/publish-version",
                "{\"note\":\"Shorter window\"}");
        assertThat(republished.getStatusCode()).as(republished.getBody()).isEqualTo(HttpStatus.OK);
        assertThat(json(republished).get("version").asInt()).isEqualTo(2);
        assertThat(json(republished).get("note").asString()).isEqualTo("Shorter window");
        JsonNode versions = json(get(P + "/policies/" + id + "/versions", true));
        assertThat(versions.size()).isEqualTo(2);
        assertThat(versions.get(0).get("status").asString()).isEqualTo("LIVE");
        assertThat(versions.get(1).get("status").asString()).isEqualTo("ARCHIVED");

        JsonNode live = json(get(SF + "/policies/RETURNS", false));
        assertThat(live.get("version").asInt()).isEqualTo(2);
        assertThat(live.get("body").asString()).contains("14 days");
        assertThat(live.get("requiresAcceptance").asBoolean()).isTrue();
        JsonNode old = json(get(SF + "/policies/RETURNS?v=1", false));
        assertThat(old.get("body").asString()).contains("30 days");

        JsonNode compliance = json(get(P + "/policies/compliance", true));
        assertThat(compliance.toString()).contains("\"type\":\"RETURNS\"").contains("\"status\":\"PUBLISHED\"");
        JsonNode site = json(get(SF + "/site", false));
        assertThat(site.get("policies").toString()).contains("RETURNS");
        JsonNode sitemap = json(get(SF + "/sitemap", false));
        assertThat(sitemap.toString()).contains("/policies/returns");
    }

    // ------------------------------------------------------------------------------------------------ menus

    @Test
    void menusBootstrapFromLegacyPagesAndRefuseDepth() throws Exception {
        JsonNode main = json(get(P + "/menus/MAIN", true));
        // the seeded store has about-us and contact-us with link_to_menu
        assertThat(main.get("items").toString()).contains("about-us").contains("contact-us");
        JsonNode resolved = json(get(SF + "/menus/MAIN", false));
        assertThat(resolved.toString()).contains("/content/about-us");

        String tree = "{\"items\":[{\"labels\":{\"en\":\"Shop\"},\"target\":{\"kind\":\"URL\",\"value\":\"/category/all\"},"
                + "\"children\":[{\"labels\":{\"en\":\"About\"},\"target\":{\"kind\":\"PAGE\",\"value\":\"about-us\"}},"
                + "{\"labels\":{\"en\":\"Gone\"},\"target\":{\"kind\":\"PAGE\",\"value\":\"no-such-page\"}}]},"
                + "{\"labels\":{\"en\":\"Blog\"},\"target\":{\"kind\":\"BLOG_INDEX\"}}]}";
        var put = send("PUT", P + "/menus/MAIN", tree);
        assertThat(put.getStatusCode()).as(put.getBody()).isEqualTo(HttpStatus.OK);
        JsonNode saved = json(put);
        assertThat(saved.get("items").size()).as(saved.toString()).isEqualTo(2);
        assertThat(saved.get("items").get(0).get("children").size()).isEqualTo(2);
        assertThat(saved.get("items").get(0).get("children").get(1).get("target").get("broken").asBoolean()).isTrue();

        JsonNode sf = json(get(SF + "/menus/MAIN", false));
        assertThat(sf.size()).isEqualTo(2);
        assertThat(sf.get(0).get("children").size()).isEqualTo(1); // the broken one is dropped
        assertThat(sf.get(1).get("href").asString()).isEqualTo("/blog");

        String tooDeep = "{\"items\":[{\"labels\":{\"en\":\"A\"},\"target\":{\"kind\":\"URL\",\"value\":\"/a\"},"
                + "\"children\":[{\"labels\":{\"en\":\"B\"},\"target\":{\"kind\":\"URL\",\"value\":\"/b\"},"
                + "\"children\":[{\"labels\":{\"en\":\"C\"},\"target\":{\"kind\":\"URL\",\"value\":\"/c\"}}]}]}]}";
        var deep = send("PUT", P + "/menus/FOOTER", tooDeep);
        assertThat(deep.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(deep).get("code").asString()).isEqualTo("CONTENT.MENU.DEPTH_EXCEEDED");
    }

    // ------------------------------------------------------------------------------------- storefront page

    @Test
    void storefrontPageReadAndPreviewToken() throws Exception {
        String slug = slug("preview");
        var created = send("POST", P + "/pages", "{\"slug\":\"" + slug + "\",\"translations\":[{\"language\":\"en\","
                + "\"title\":\"Draft page\",\"body\":\"<p>secret</p>\",\"metaDescription\":\"md\"}]}");
        long id = json(created).get("id").asLong();
        assertThat(get(SF + "/pages/" + slug, false).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JsonNode token = json(send("POST", P + "/pages/" + id + "/preview-token", null));
        var preview = get(SF + "/pages/" + slug + "?preview=" + token.get("token").asString(), false);
        assertThat(preview.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(preview).get("body").asString()).isEqualTo("<p>secret</p>");
        assertThat(json(preview).get("seo").get("metaDescription").asString()).isEqualTo("md");
        assertThat(get(SF + "/pages/" + slug + "?preview=bogus.token", false).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        // legacy seeded page still reads through the new surface, in the requested locale
        var about = client().get().uri("/api/v1/storefront/pages/about-us?store=" + STORE + "&lang=ar")
                .retrieve().toEntity(String.class);
        assertThat(about.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(about).get("servedLocale").asString()).isEqualTo("ar");
        assertThat(about.getHeaders().getCacheControl()).contains("max-age=60");
    }

}
