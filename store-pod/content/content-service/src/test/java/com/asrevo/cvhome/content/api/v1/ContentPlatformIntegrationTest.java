package com.asrevo.cvhome.content.api.v1;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

import com.asrevo.cvhome.content.TestClockConfiguration;
import com.asrevo.cvhome.content.TestcontainersConfiguration;
import com.asrevo.cvhome.s2s.config.ServletTestCustomSecurityConfig;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.BODY;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CODE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.CONTENT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STATUS;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STOREFRONT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.TITLE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.VERSION;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.query;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The phase-2 domains end to end: media (dedupe, type rules, delete guard), banners (capacity, effective), FAQ
 * (groups, reorder, storefront + JSON-LD), policies (version cut on publish, storefront read), posts, menus and the
 * aggregated storefront site document.
 */
@Import({TestcontainersConfiguration.class, TestClockConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"signer", "test-stores"})
@Tag("integration-test")
class ContentPlatformIntegrationTest {

    private static final String STORE = "65f023632bc46470c104b76f";

    private static final String MEDIA_SEGMENT = "media";

    private static final String MEDIA = path(PRIVATE, MEDIA_SEGMENT);

    private static final String POSTS = "posts";

    private static final String SITE = "site";

    private static final String GROUPS = "groups";

    private static final String VERSIONS = "versions";

    private static final String HERO_SLUG = "hero";

    private static final String HERO_QUERY = "placement=HERO";

    private static final String STRIP_TITLE = "Free delivery over 250";

    private static final String QUESTION = "How fast?";

    private static final String RETURNS_SLUG = "returns";

    private static final String THIRTY_DAYS = "30 days";

    private static final String FOURTEEN_DAYS = "14 days";

    private static final String BANNERS = "banners";

    private static final String FAQ = "faq";

    private static final String POLICIES = "policies";

    private static final String PAGES = "pages";

    private static final String MENUS = "menus";

    private static final String MAIN = "MAIN";

    private static final String ITEMS = "items";

    private static final String CHILDREN = "children";

    private static final String KIND = "kind";

    private static final String KEY = "key";

    private static final String SHIPPING = "shipping";

    private static final String ORDERING = "ordering";

    private static final String PIXEL_PNG = "pixel.png";

    private static final String ABOUT_US = "about-us";

    private static final String HERO = "HERO";

    private static final String RETURNS = "RETURNS";

    private static final String PUBLISH = "publish";

    private static final String BANNER = """
            {"slug":"%s","placement":"%s","target":{"kind":"URL","value":"/x"},
             "translations":[{"language":"en","title":"%s","subtitle":"sub","ctaLabel":"Go"}]}""";

    private static final String FAQ_ENTRY = """
            {"slug":"%s","groupId":%d,"keywords":["delivery"],"showInCheckoutHelp":true,
             "translations":[{"language":"en","title":"%s","body":"<p>%s</p>"}]}""";

    private static final String POLICY = """
            {"slug":"%s","policyType":"RETURNS","jurisdiction":"NL","requiresAcceptance":true,
             "translations":[{"language":"en","title":"Returns","body":"<p>%s</p>"}]}""";

    private static final String MENU_TREE = """
            {"items":[{"labels":{"en":"Shop"},"target":{"kind":"URL","value":"/category/all"},"children":[
                {"labels":{"en":"About"},"target":{"kind":"PAGE","value":"about-us"}},
                {"labels":{"en":"Gone"},"target":{"kind":"PAGE","value":"no-such-page"}}]},
              {"labels":{"en":"Blog"},"target":{"kind":"BLOG_INDEX"}}]}""";

    private static final String MENU_TOO_DEEP = """
            {"items":[{"labels":{"en":"A"},"target":{"kind":"URL","value":"/a"},"children":[
                {"labels":{"en":"B"},"target":{"kind":"URL","value":"/b"},"children":[
                    {"labels":{"en":"C"},"target":{"kind":"URL","value":"/c"}}]}]}]}""";

    // 1x1 PNG
    private static final byte[] PNG = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");

    @LocalServerPort
    private int port;

    @Autowired
    private ServletTestCustomSecurityConfig.JwtSigner signer;

    private ApiTestSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiTestSupport(port, signer);
        admin = api.token(ROLE_STORE_ADMIN, STORE);
    }

    // ---------------------------------------------------------------------------------------------- helpers

    private ResponseEntity<String> get(String path) {
        return api.get(scoped(path, STORE), admin);
    }

    private ResponseEntity<String> getPublic(String path) {
        return api.get(scoped(path, STORE), null);
    }

    private ResponseEntity<String> send(HttpMethod method, String path, String body) {
        return api.send(method, scoped(path, STORE), admin, body);
    }

    private long createAndPublish(String type, String body) {
        var created = send(HttpMethod.POST, path(PRIVATE, type), body);
        expect(created, HttpStatus.CREATED);
        long id = json(created).get(ID).asLong();
        expect(send(HttpMethod.POST, path(PRIVATE, type, id, PUBLISH), null), HttpStatus.OK);
        return id;
    }

    private static String paragraph(String word, int times) {
        return String.format("<p>%s</p>", word.repeat(times));
    }

    // ------------------------------------------------------------------------------------------------ media

    @Test
    void mediaUploadDedupesAndGuardsDelete() {
        var first = api.upload(scoped(MEDIA, STORE), admin, PIXEL_PNG, PNG);
        expect(first, HttpStatus.CREATED);
        JsonNode asset = json(first).get(0);
        long id = asset.get(ID).asLong();
        assertThat(asset.get(KIND).asString()).isEqualTo("IMAGE");
        assertThat(asset.get("width").asInt()).isEqualTo(1);
        assertThat(asset.get("url").asString()).contains(path("", "files", STORE, MEDIA_SEGMENT, id, PIXEL_PNG));

        // same bytes again → the same asset, nothing stored twice
        var again = api.upload(scoped(MEDIA, STORE), admin, "copy.png", PNG);
        assertThat(json(again).get(0).get(ID).asLong()).isEqualTo(id);

        // a type we do not accept
        var exe = api.upload(scoped(MEDIA, STORE), admin, "x.exe", "MZ".getBytes(StandardCharsets.UTF_8));
        expect(exe, HttpStatus.BAD_REQUEST);
        assertThat(json(exe).get(CODE).asString()).isEqualTo("MEDIA.TYPE_NOT_ALLOWED");

        // svg is sanitised
        var svg = api.upload(scoped(MEDIA, STORE), admin, "logo.svg", """
                <svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script><rect onclick="x()"/></svg>"""
                .getBytes(StandardCharsets.UTF_8));
        expect(svg, HttpStatus.CREATED);
        assertThat(json(svg).get(0).get(KIND).asString()).isEqualTo("VECTOR");

        // folders exist by default, list filters
        assertThat(json(get(path(MEDIA, "folders"))).size()).isGreaterThanOrEqualTo(5);
        assertThat(json(get(query(MEDIA, "kind=IMAGE"))).get(CONTENT).size()).isGreaterThanOrEqualTo(1);

        // reference it from a post, then deleting is a 409 until forced
        String postSlug = slug("post");
        long postId = createAndPublish(POSTS, String.format("""
                {"slug":"%s","heroMediaId":%d,"tags":["news"],
                 "translations":[{"language":"en","title":"Hello","body":"%s"}]}""", postSlug, id,
                paragraph("word ", 450)));
        JsonNode usage = json(get(path(MEDIA, id, "usage")));
        assertThat(usage.size()).isEqualTo(1);
        assertThat(usage.get(0).get("itemId").asLong()).isEqualTo(postId);
        var del = send(HttpMethod.DELETE, path(MEDIA, id), null);
        expect(del, HttpStatus.CONFLICT);
        assertThat(json(del).get(CODE).asString()).isEqualTo("MEDIA.REFERENCED");

        // the storefront post carries the hero url and reading minutes
        JsonNode sfPost = json(getPublic(path(STOREFRONT, POSTS, postSlug)));
        assertThat(sfPost.get("heroImageUrl").asString()).contains(PIXEL_PNG);
        assertThat(sfPost.get("readingMinutes").asInt()).isEqualTo(3);
        assertThat(sfPost.get("tags").get(0).asString()).isEqualTo("news");
        JsonNode sfList = json(getPublic(query(path(STOREFRONT, POSTS), "tag=news")));
        assertThat(sfList.get(CONTENT).size()).isGreaterThanOrEqualTo(1);

        expect(send(HttpMethod.DELETE, query(path(MEDIA, id), "force=true"), null), HttpStatus.NO_CONTENT);
        expect(get(path(MEDIA, id)), HttpStatus.NOT_FOUND);
        JsonNode summary = json(get(path(PRIVATE, "summary")));
        assertThat(summary.get(MEDIA_SEGMENT).get("fileCount").asLong()).isGreaterThanOrEqualTo(1);
    }

    // ---------------------------------------------------------------------------------------------- banners

    @Test
    void heroPlacementHoldsOneBannerAndStripFeedsTheAnnouncement() {
        long hero1 = createAndPublish(BANNERS, String.format(BANNER, slug(HERO_SLUG), HERO, "Hero one"));
        var second = send(HttpMethod.POST, path(PRIVATE, BANNERS), String.format(BANNER, slug(HERO_SLUG), HERO, "Hero two"));
        long hero2 = json(second).get(ID).asLong();
        var publish2 = send(HttpMethod.POST, path(PRIVATE, BANNERS, hero2, PUBLISH), null);
        expect(publish2, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(publish2.getBody()).contains("CONTENT.BANNER.CAPACITY_EXCEEDED");

        JsonNode effective = json(get(query(path(PRIVATE, BANNERS, "effective"), HERO_QUERY)));
        assertThat(effective.size()).isEqualTo(1);
        assertThat(effective.get(0).get(ID).asLong()).isEqualTo(hero1);

        createAndPublish(BANNERS, String.format(BANNER, slug("strip"), "STRIP", STRIP_TITLE));
        JsonNode site = json(getPublic(path(STOREFRONT, SITE)));
        assertThat(site.get("announcement").get(TITLE).asString()).isEqualTo(STRIP_TITLE);
        assertThat(site.get(MENUS).has("main")).isTrue();
        assertThat(site.get("footerPages").size()).isGreaterThanOrEqualTo(1);
        JsonNode sfBanners = json(getPublic(query(path(STOREFRONT, BANNERS), HERO_QUERY)));
        assertThat(sfBanners.get(0).get("ctaLabel").asString()).isEqualTo("Go");

        // the console row subtitle names the placement and target
        JsonNode rows = json(get(query(path(PRIVATE, BANNERS), "status=PUBLISHED")));
        assertThat(rows.get(CONTENT).toString()).contains("hero · /x");
    }

    // -------------------------------------------------------------------------------------------------- faq

    @Test
    void faqGroupsReorderAndRenderWithJsonLd() {
        JsonNode groups = json(get(path(PRIVATE, FAQ, GROUPS)));
        assertThat(groups.size()).isGreaterThanOrEqualTo(4);
        long shipping = -1;
        long ordering = -1;
        for (JsonNode g : groups) {
            if (SHIPPING.equals(g.get(KEY).asString())) {
                shipping = g.get(ID).asLong();
            }
            if (ORDERING.equals(g.get(KEY).asString())) {
                ordering = g.get(ID).asLong();
            }
        }
        long a = createAndPublish(FAQ, String.format(FAQ_ENTRY, slug(FAQ), shipping, QUESTION, "Next day."));
        long b = createAndPublish(FAQ, String.format(FAQ_ENTRY, slug(FAQ), shipping, "Weekends?", "Yes."));

        // move b to the front of ordering, a stays in shipping
        var reorder = send(HttpMethod.PATCH, path(PRIVATE, FAQ, "reorder"),
                String.format("[{\"id\":%d,\"groupId\":%d,\"position\":0}]", b, ordering));
        expect(reorder, HttpStatus.NO_CONTENT);
        JsonNode readB = json(get(path(PRIVATE, FAQ, b)));
        assertThat(readB.get("groupId").asLong()).isEqualTo(ordering);
        assertThat(readB.get("position").asInt()).isZero();
        assertThat(readB.get("groupName").asString()).isEqualTo("Ordering");

        JsonNode sf = json(getPublic(path(STOREFRONT, FAQ)));
        assertThat(sf.get("jsonLd").asString()).contains("\"@type\":\"FAQPage\"").contains(QUESTION);
        boolean found = false;
        for (JsonNode g : sf.get(GROUPS)) {
            for (JsonNode e : g.get("entries")) {
                if (e.get(ID).asLong() == a) {
                    found = SHIPPING.equals(g.get(KEY).asString());
                }
            }
        }
        assertThat(found).isTrue();
        JsonNode rows = json(get(path(PRIVATE, FAQ)));
        assertThat(rows.get(CONTENT).toString()).contains("position 1");
    }

    // ---------------------------------------------------------------------------------------------- policies

    @Test
    void publishingAPolicyCutsVersionsTheStorefrontServes() {
        String slug = slug(RETURNS_SLUG);
        String base = path(PRIVATE, POLICIES);
        // a template exists
        JsonNode template = json(get(query(path(base, "templates"), "type=RETURNS&jurisdiction=NL")));
        assertThat(template.get("translations").size()).isEqualTo(2);

        long id = createAndPublish(POLICIES, String.format(POLICY, slug, THIRTY_DAYS));
        String one = path(base, id);
        JsonNode read = json(get(one));
        assertThat(read.get("liveVersion").asInt()).isEqualTo(1);
        assertThat(read.get(VERSIONS).size()).isEqualTo(1);

        // a second head of the same type is refused
        var dup = send(HttpMethod.POST, base, String.format(POLICY, slug(RETURNS_SLUG), "x"));
        expect(dup, HttpStatus.CONFLICT);
        assertThat(json(dup).get(CODE).asString()).isEqualTo("CONTENT.POLICY.TYPE_ACTIVE_EXISTS");

        // edit the text and publish again → version 2 live, version 1 archived, both readable
        int version = read.get(VERSION).asInt();
        String edited = String.format(POLICY, slug, FOURTEEN_DAYS)
                .replace("{\"slug\"", String.format("{\"version\":%d,\"slug\"", version));
        expect(send(HttpMethod.PUT, one, edited), HttpStatus.OK);
        var republished = send(HttpMethod.POST, path(one, "publish-version"), "{\"note\":\"Shorter window\"}");
        expect(republished, HttpStatus.OK);
        assertThat(json(republished).get(VERSION).asInt()).isEqualTo(2);
        assertThat(json(republished).get("note").asString()).isEqualTo("Shorter window");
        JsonNode versions = json(get(path(one, VERSIONS)));
        assertThat(versions.size()).isEqualTo(2);
        assertThat(versions.get(0).get(STATUS).asString()).isEqualTo("LIVE");
        assertThat(versions.get(1).get(STATUS).asString()).isEqualTo("ARCHIVED");

        String sfPolicy = path(STOREFRONT, POLICIES, RETURNS);
        JsonNode live = json(getPublic(sfPolicy));
        assertThat(live.get(VERSION).asInt()).isEqualTo(2);
        assertThat(live.get(BODY).asString()).contains(FOURTEEN_DAYS);
        assertThat(live.get("requiresAcceptance").asBoolean()).isTrue();
        JsonNode old = json(getPublic(query(sfPolicy, "v=1")));
        assertThat(old.get(BODY).asString()).contains(THIRTY_DAYS);

        JsonNode compliance = json(get(path(base, "compliance")));
        assertThat(compliance.toString()).contains("\"type\":\"RETURNS\"").contains("\"status\":\"PUBLISHED\"");
        assertThat(json(getPublic(path(STOREFRONT, SITE))).get(POLICIES).toString()).contains(RETURNS);
        assertThat(json(getPublic(path(STOREFRONT, "sitemap"))).toString()).contains("/policies/returns");
    }

    // ------------------------------------------------------------------------------------------------ menus

    @Test
    void menusBootstrapFromLegacyPagesAndRefuseDepth() {
        String mainMenu = path(PRIVATE, MENUS, MAIN);
        String sfMainMenu = path(STOREFRONT, MENUS, MAIN);
        JsonNode main = json(get(mainMenu));
        // the seeded store has about-us and contact-us with link_to_menu
        assertThat(main.get(ITEMS).toString()).contains(ABOUT_US).contains("contact-us");
        assertThat(json(getPublic(sfMainMenu)).toString()).contains("/content/about-us");

        var put = send(HttpMethod.PUT, mainMenu, MENU_TREE);
        expect(put, HttpStatus.OK);
        JsonNode saved = json(put);
        assertThat(saved.get(ITEMS).size()).as(saved.toString()).isEqualTo(2);
        assertThat(saved.get(ITEMS).get(0).get(CHILDREN).size()).isEqualTo(2);
        assertThat(saved.get(ITEMS).get(0).get(CHILDREN).get(1).get("target").get("broken").asBoolean()).isTrue();

        JsonNode sf = json(getPublic(sfMainMenu));
        assertThat(sf.size()).isEqualTo(2);
        assertThat(sf.get(0).get(CHILDREN).size()).isEqualTo(1); // the broken one is dropped
        assertThat(sf.get(1).get("href").asString()).isEqualTo("/blog");

        var deep = send(HttpMethod.PUT, path(PRIVATE, MENUS, "FOOTER"), MENU_TOO_DEEP);
        expect(deep, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(deep).get(CODE).asString()).isEqualTo("CONTENT.MENU.DEPTH_EXCEEDED");
    }

    // ------------------------------------------------------------------------------------- storefront page

    @Test
    void storefrontPageReadAndPreviewToken() {
        String slug = slug("preview");
        var created = send(HttpMethod.POST, path(PRIVATE, PAGES), String.format("""
                {"slug":"%s","translations":[{"language":"en","title":"Draft page","body":"<p>secret</p>",
                 "metaDescription":"md"}]}""", slug));
        long id = json(created).get(ID).asLong();
        String sfPage = path(STOREFRONT, PAGES, slug);
        expect(getPublic(sfPage), HttpStatus.NOT_FOUND);
        JsonNode token = json(send(HttpMethod.POST, path(PRIVATE, PAGES, id, "preview-token"), null));
        var preview = getPublic(query(sfPage, String.format("preview=%s", token.get("token").asString())));
        expect(preview, HttpStatus.OK);
        assertThat(json(preview).get(BODY).asString()).isEqualTo("<p>secret</p>");
        assertThat(json(preview).get("seo").get("metaDescription").asString()).isEqualTo("md");
        expect(getPublic(query(sfPage, "preview=bogus.token")), HttpStatus.NOT_FOUND);
        // legacy seeded page still reads through the new surface, in the requested locale
        var about = api.get(String.format("%s?store=%s&lang=ar", path(STOREFRONT, PAGES, ABOUT_US), STORE), null);
        expect(about, HttpStatus.OK);
        assertThat(json(about).get("servedLocale").asString()).isEqualTo("ar");
        assertThat(about.getHeaders().getCacheControl()).contains("max-age=60");
        assertThat(json(get(path(PRIVATE, PAGES, id))).get(STATUS).asString()).isEqualTo("DRAFT");
    }

}
