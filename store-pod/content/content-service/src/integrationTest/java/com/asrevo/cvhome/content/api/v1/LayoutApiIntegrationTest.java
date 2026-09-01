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

import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.EN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ID;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.PRIVATE;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.STOREFRONT;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.expect;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.json;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.path;
import static com.asrevo.cvhome.content.api.v1.ApiTestSupport.scoped;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The layout document's whole lifecycle over HTTP: draft saves under the optimistic version, publish with
 * revisions, restore, discard, the preview-token gate on the storefront's draft read, sanitisation on write,
 * the section-preset library, and tenant/role isolation. The stores are seeded with a published home layout,
 * so every test starts from the version its own GET reports rather than assuming a fresh row.
 */
@StorageIntegrationTest
class LayoutApiIntegrationTest {

    /** Seeded store (languages ar, fr). */
    private static final String STORE_A = "65f023632bc26470c104b75f";

    /** Another seeded store (languages en, fr). */
    private static final String STORE_B = "65f023632bc46470c104b75f";

    private static final String LAYOUTS = "layouts";

    private static final String HOME = path(PRIVATE, LAYOUTS, "HOME");

    private static final String PRESETS = path(PRIVATE, LAYOUTS, "section-presets");

    private static final String STOREFRONT_LAYOUT = "%s/layout/HOME?store=%s&lang=%s";

    private static final String META = "meta";

    private static final String DRAFT_VERSION = "draftVersion";

    private static final String DRAFT = "draft";

    private static final String SECTIONS = "sections";

    private static final String DIRTY = "dirty";

    private static final String HEADING = "heading";

    private static final String TEXT = "text";

    private static final String TOKEN = "token";

    private static final String BASE_VERSION_BODY = "{\"baseVersion\":%d}";

    private static final String DOC_BODY = """
            {"baseVersion":%d,
             "document":{"schemaVersion":1,"page":"HOME","sections":[%s]}}""";

    private static final String HERO = """
            {"id":"sec-hero","kind":"hero","variant":"minimal",
             "props":{"height":"md"},
             "text":{"heading":{"en":"%s","ar":"عنوان","fr":"Titre"}},
             "style":{"spacing":"lg","width":"full","tone":"default"},
             "visibility":{"hidden":false}}""";

    private static final String HIDDEN_RICHTEXT = """
            {"id":"sec-hidden","kind":"richtext","variant":"centered",
             "text":{"body":{"en":"<p>backstage</p>"}},
             "visibility":{"hidden":true}}""";

    private static final String PUBLISH = "publish";

    private static final String DISCARD = "discard";

    private static final String REVISIONS = "revisions";

    private static final String RESTORE = "restore";

    private static final String PREVIEW_TOKEN = "preview-token";

    private static final String LIVE_HEADING = "Live heading";

    private static final String EDITED = "Edited";

    private static final String BACKSTAGE_HEADING = "Backstage heading";

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

    private static String hero(String heading) {
        return String.format(HERO, heading);
    }

    private JsonNode getLayout(String store, String token) {
        var r = api.get(scoped(HOME, store), token);
        expect(r, HttpStatus.OK);
        return json(r);
    }

    private int baseVersion(String store, String token) {
        return getLayout(store, token).get(META).get(DRAFT_VERSION).asInt();
    }

    private ResponseEntity<String> save(String store, String token, int base, String sectionsJson) {
        return api.send(HttpMethod.PUT, scoped(HOME, store), token, String.format(DOC_BODY, base, sectionsJson));
    }

    private ResponseEntity<String> post(String store, String token, String action, int base) {
        return api.send(HttpMethod.POST, scoped(path(HOME, action), store), token,
                String.format(BASE_VERSION_BODY, base));
    }

    private JsonNode storefrontLayout(String store, String lang, String preview) {
        String url = String.format(STOREFRONT_LAYOUT, STOREFRONT, store, lang);
        if (preview != null) {
            url = String.format("%s&preview=%s", url, preview);
        }
        var r = api.get(url, null);
        expect(r, HttpStatus.OK);
        return json(r);
    }

    private static String heading(JsonNode storefrontDoc) {
        for (JsonNode section : storefrontDoc.get(SECTIONS)) {
            if ("hero".equals(section.get("kind").asString())) {
                return section.get(TEXT).get(HEADING).asString();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------------------------------- tests

    @Test
    void draftLifecycleWithOptimisticVersions() {
        int base = baseVersion(STORE_A, admin);

        // a stale base is a conflict, not a clobber
        expect(save(STORE_A, admin, base - 1, hero("stale")), HttpStatus.CONFLICT);

        var saved = save(STORE_A, admin, base, hero(EDITED));
        expect(saved, HttpStatus.OK);
        JsonNode layout = json(saved);
        assertThat(layout.get(META).get(DRAFT_VERSION).asInt()).isEqualTo(base + 1);
        assertThat(layout.get(META).get(DIRTY).asBoolean()).isTrue();
        assertThat(layout.get(DRAFT).get(SECTIONS).get(0).get(TEXT).get(HEADING).get(EN).asString())
                .isEqualTo(EDITED);

        // publish; a repeat publish of the same version is a no-op, not a duplicate-revision error
        expect(post(STORE_A, admin, PUBLISH, base + 1), HttpStatus.OK);
        var again = post(STORE_A, admin, PUBLISH, base + 1);
        expect(again, HttpStatus.OK);
        assertThat(json(again).get(META).get(DIRTY).asBoolean()).isFalse();

        // the publish is a revision row; restoring it re-materialises the document into the draft
        var revisions = api.get(scoped(path(HOME, REVISIONS), STORE_A), admin);
        expect(revisions, HttpStatus.OK);
        int version = json(revisions).get(0).get("version").asInt();
        assertThat(version).isEqualTo(base + 1);
        var restored = api.send(HttpMethod.POST,
                scoped(path(HOME, REVISIONS, version, RESTORE), STORE_A), admin, null);
        expect(restored, HttpStatus.OK);
        expect(api.send(HttpMethod.POST,
                scoped(path(HOME, REVISIONS, 999999, RESTORE), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);

        // discard: the published document becomes the draft again
        int current = json(restored).get(META).get(DRAFT_VERSION).asInt();
        var discarded = post(STORE_A, admin, DISCARD, current);
        expect(discarded, HttpStatus.OK);
        assertThat(json(discarded).get(META).get(DIRTY).asBoolean()).isFalse();
    }

    @Test
    void unrenderableDocumentsAreRefused() {
        int base = baseVersion(STORE_A, admin);
        expect(save(STORE_A, admin, base, "{\"id\":\"sec-x\",\"kind\":\"no-such-kind\"}"),
                HttpStatus.BAD_REQUEST);
        expect(save(STORE_A, admin, base,
                "{\"id\":\"dup\",\"kind\":\"richtext\"},{\"id\":\"dup\",\"kind\":\"richtext\"}"),
                HttpStatus.BAD_REQUEST);
    }

    @Test
    void richtextBodiesAreSanitizedOnWrite() {
        int base = baseVersion(STORE_A, admin);
        var saved = save(STORE_A, admin, base, """
                {"id":"sec-story","kind":"richtext",
                 "text":{"body":{"en":"<p>fine</p><script>alert(1)</script>"}}}""");
        expect(saved, HttpStatus.OK);
        String body = json(saved).get(DRAFT).get(SECTIONS).get(0).get(TEXT).get("body").get(EN).asString();
        assertThat(body).contains("<p>fine</p>").doesNotContain("script");
    }

    @Test
    void publishBlocksOnAMediaReferenceTheLibraryDoesNotHold() {
        int base = baseVersion(STORE_A, admin);
        expect(save(STORE_A, admin, base,
                "{\"id\":\"sec-img\",\"kind\":\"image\",\"props\":{\"mediaId\":999999}}"), HttpStatus.OK);
        expect(post(STORE_A, admin, PUBLISH, base + 1), HttpStatus.UNPROCESSABLE_CONTENT);
        // leave the draft consistent for the other tests
        expect(post(STORE_A, admin, DISCARD, base + 1), HttpStatus.OK);
    }

    @Test
    void storefrontServesTheDraftOnlyToAValidPreviewToken() {
        String adminB = api.token(ROLE_STORE_ADMIN, STORE_B);
        int base = baseVersion(STORE_B, adminB);
        expect(save(STORE_B, adminB, base, hero(LIVE_HEADING)), HttpStatus.OK);
        expect(post(STORE_B, adminB, PUBLISH, base + 1), HttpStatus.OK);
        expect(save(STORE_B, adminB, base + 1,
                String.format("%s,%s", hero(BACKSTAGE_HEADING), HIDDEN_RICHTEXT)), HttpStatus.OK);

        // published copy for shoppers; hidden sections never leave the building
        JsonNode livePage = storefrontLayout(STORE_B, EN, null);
        assertThat(heading(livePage)).isEqualTo(LIVE_HEADING);

        var minted = api.send(HttpMethod.POST, scoped(path(HOME, PREVIEW_TOKEN), STORE_B), adminB, null);
        expect(minted, HttpStatus.OK);
        String token = json(minted).get(TOKEN).asString();

        JsonNode draftPage = storefrontLayout(STORE_B, EN, token);
        assertThat(heading(draftPage)).isEqualTo(BACKSTAGE_HEADING);
        assertThat(draftPage.get(SECTIONS).findValues(ID)).noneMatch(id -> "sec-hidden".equals(id.asString()));

        // a garbage token, or another store's token, falls back to the published copy
        assertThat(heading(storefrontLayout(STORE_B, EN, "not-a-token"))).isEqualTo(LIVE_HEADING);
        var mintedA = api.send(HttpMethod.POST, scoped(path(HOME, PREVIEW_TOKEN), STORE_A), admin, null);
        assertThat(heading(storefrontLayout(STORE_B, EN, json(mintedA).get(TOKEN).asString())))
                .isEqualTo(LIVE_HEADING);

        // copy flattens to the requested language, falling back without ever going blank
        assertThat(heading(storefrontLayout(STORE_B, "fr", null))).isEqualTo("Titre");
        assertThat(heading(storefrontLayout(STORE_B, "ru", null))).isNotBlank();
    }

    @Test
    void sectionPresetsRoundTripScopedToTheirStore() {
        var savedPreset = api.send(HttpMethod.POST, scoped(PRESETS, STORE_A), admin, """
                {"name":"My hero","section":{"id":"sec-hero","kind":"hero","variant":"minimal",
                 "text":{"heading":{"en":"Welcome"}}}}""");
        expect(savedPreset, HttpStatus.OK);
        long id = json(savedPreset).get(ID).asLong();

        var listed = api.get(scoped(PRESETS, STORE_A), admin);
        expect(listed, HttpStatus.OK);
        assertThat(json(listed).findValues(ID)).anyMatch(node -> node.asLong() == id);

        expect(api.send(HttpMethod.POST, scoped(PRESETS, STORE_A), admin,
                "{\"name\":\"Bad\",\"section\":{\"id\":\"x\",\"kind\":\"no-such-kind\"}}"),
                HttpStatus.BAD_REQUEST);

        // another store can neither see nor delete it
        String adminB = api.token(ROLE_STORE_ADMIN, STORE_B);
        var otherList = api.get(scoped(PRESETS, STORE_B), adminB);
        assertThat(json(otherList).findValues(ID)).noneMatch(node -> node.asLong() == id);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRESETS, id), STORE_B), adminB, null),
                HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(PRESETS, id), STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        expect(api.send(HttpMethod.DELETE, scoped(path(PRESETS, id), STORE_A), admin, null),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void anotherStoresTokenIsForbiddenAndModeratorsCannotWrite() {
        String adminB = api.token(ROLE_STORE_ADMIN, STORE_B);
        expect(api.get(scoped(HOME, STORE_A), adminB), HttpStatus.FORBIDDEN);

        String moderator = api.token(ROLE_STORE_MODERATOR, STORE_A);
        expect(api.get(scoped(HOME, STORE_A), moderator), HttpStatus.OK);
        int base = baseVersion(STORE_A, admin);
        expect(save(STORE_A, moderator, base, hero("nope")), HttpStatus.FORBIDDEN);
    }

}
