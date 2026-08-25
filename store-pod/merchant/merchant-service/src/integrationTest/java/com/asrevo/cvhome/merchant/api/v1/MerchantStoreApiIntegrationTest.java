package com.asrevo.cvhome.merchant.api.v1;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;
import com.asrevo.cvhome.testsupport.security.Tokens;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.testsupport.http.ApiClient.expect;
import static com.asrevo.cvhome.testsupport.http.ApiClient.json;
import static com.asrevo.cvhome.testsupport.http.ApiClient.path;
import static com.asrevo.cvhome.testsupport.http.ApiClient.scoped;
import static com.asrevo.cvhome.testsupport.http.ApiClient.slug;
import static com.asrevo.cvhome.testsupport.security.Tokens.ROLE_STORE_ADMIN;
import static com.asrevo.cvhome.testsupport.security.Tokens.ROLE_STORE_MODERATOR;
import static com.asrevo.cvhome.testsupport.security.Tokens.SCOPE_STORE_CORE;
import static com.asrevo.cvhome.testsupport.security.Tokens.STORE_1;
import static com.asrevo.cvhome.testsupport.security.Tokens.STORE_2;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The store API over real HTTP against Postgres and MinIO: the public reads, the full create/update/upload/delete
 * life cycle of a store, tenant isolation between stores, and the permission gates on every private endpoint.
 */
@StorageIntegrationTest
@TestPropertySource(properties = {
        "com.asrevo.cvhome.pod-info.pod.name=pod-507f1f77",
        "com.asrevo.cvhome.pod-info.pod.domain=spg-507f1f77.gateway.com"})
class MerchantStoreApiIntegrationTest {

    private static final String API = "/api/v1";

    private static final String STORE_SEGMENT = "store";

    private static final String STORE = path(API, STORE_SEGMENT);

    private static final String PRIVATE_STORE = path(API, "private", STORE_SEGMENT);

    private static final String LANGUAGES = path(STORE, "languages");

    private static final String SOCIAL_LINKS = path(PRIVATE_STORE, "social-links");

    private static final String MARKETING = path(PRIVATE_STORE, "marketing");

    private static final String LOGO_FIELD = "logo";

    private static final String BANNER_FIELD = "banner";

    private static final String LOGO = path(MARKETING, LOGO_FIELD);

    private static final String BANNER = path(MARKETING, BANNER_FIELD);

    private static final String ADD_SLIDER = path(MARKETING, "add-slider-image");

    private static final String SLIDER_IMAGES = path(MARKETING, "slider-images");

    private static final String CODE = "code";

    private static final String NAME = "name";

    private static final String ID = "id";

    private static final String PNG = "image/png";

    private static final String SLIDER_IMAGES_FIELD = "sliderImages";

    private static final String STORE_DOMAINS_FIELD = "storeDomains";

    private static final String DOMAIN_FIELD = "domain";

    private static final String PATH_FIELD = "path";

    private static final String LOGO_FILE = "logo.png";

    private static final byte[] PIXEL = {(byte) 0x89, 'P', 'N', 'G', 0, 1, 2, 3};

    private static final String STORE_BODY = """
            {"id":"%s","name":"%s","org":"%s","email":"owner@%s.test","phone":"555 0100",
             "theme":"BASIS","colorTheme":"DEFAULT","currency":"USD","defaultLanguage":"en",
             "countryIsoCode":"US","supportedLanguages":["en","fr"],"inBusinessSince":"2024-01-01",
             "dimension":"CM","weight":"KG",
             "address":{"country":"US","stateProvince":"CA","address":"1 Market St","postalCode":"94105",
                        "city":"San Francisco"}}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private ApiClient api;

    private Tokens tokens;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new ApiClient(port);
        tokens = new Tokens(signer);
        admin = tokens.staff(ROLE_STORE_ADMIN, STORE_1);
    }

    // ------------------------------------------------------------------------------------------------ helpers

    private static String storeBody(String id, String name) {
        return String.format(STORE_BODY, id, name, Tokens.ORG_1, name);
    }

    private ResponseEntity<String> publicRead(String pathStore, String tenantStore) {
        return api.get(scoped(path(STORE, pathStore), tenantStore), null);
    }

    private ResponseEntity<String> privateRead(String store, String token) {
        return api.get(scoped(PRIVATE_STORE, store), token);
    }

    private String createStore(String name) {
        String id = new ObjectId().toHexString();
        expect(api.send(HttpMethod.POST, PRIVATE_STORE, tokens.superAdmin(), storeBody(id, name)), HttpStatus.OK);
        return id;
    }

    private ResponseEntity<String> upload(String url, String token, String filename) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("file", new ByteArrayResource(PIXEL) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        return RestClient.builder().baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(s -> true, (req, res) -> { })
                .defaultHeader(HttpHeaders.CONTENT_TYPE, PNG)
                .build()
                .post().uri(url)
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
                .contentType(MediaType.MULTIPART_FORM_DATA).body(form)
                .retrieve().toEntity(String.class);
    }

    // ------------------------------------------------------------------------------------------- public reads

    @Test
    void storefrontReadsTheSeededStore() {
        ResponseEntity<String> response = publicRead(STORE_1, STORE_1);

        expect(response, HttpStatus.OK);
        JsonNode store = json(response);
        assertThat(store.get(ID).asString()).isEqualTo(STORE_1);
        assertThat(store.get(NAME).asString()).isNotBlank();
        assertThat(store.get("supportedLanguages")).hasSize(2);
        assertThat(store.get(SLIDER_IMAGES_FIELD)).hasSize(5);
        assertThat(store.get(LOGO_FIELD).get(PATH_FIELD).asString()).contains(String.format("/files/%s/LOGO/", STORE_1));
    }

    @Test
    void storefrontReadRefusesAPathThatDisagreesWithTheTenant() {
        ResponseEntity<String> response = publicRead(STORE_2, STORE_1);

        expect(response, HttpStatus.BAD_REQUEST);
        assertThat(json(response).get(CODE).asString()).isEqualTo("MERCHANT.STORE.CONTEXT_MISMATCH");
    }

    @Test
    void storefrontReadOfAnUnknownStoreIsNotFound() {
        String unknown = new ObjectId().toHexString();

        ResponseEntity<String> response = publicRead(unknown, unknown);

        expect(response, HttpStatus.NOT_FOUND);
        assertThat(json(response).get(CODE).asString()).isEqualTo("MERCHANT.STORE.NOT_FOUND");
    }

    @Test
    void peerReadAndLanguagesNeedNoToken() {
        ResponseEntity<String> peer = api.get(scoped(STORE, STORE_1), null);
        expect(peer, HttpStatus.OK);
        assertThat(json(peer).get(ID).asString()).isEqualTo(STORE_1);

        ResponseEntity<String> languages = api.get(scoped(LANGUAGES, STORE_1), null);
        expect(languages, HttpStatus.OK);
        assertThat(languages.getBody()).contains("ar").contains("en");
    }

    // --------------------------------------------------------------------------------- private read + gates

    @Test
    void storeStaffAndPlatformCanReadTheFullStore() {
        expect(privateRead(STORE_1, admin), HttpStatus.OK);
        expect(privateRead(STORE_1, tokens.staff(ROLE_STORE_MODERATOR, STORE_1)), HttpStatus.OK);
        expect(privateRead(STORE_1, tokens.orgAdmin(Tokens.ORG_1)), HttpStatus.OK);
        expect(privateRead(STORE_1, tokens.s2s(SCOPE_STORE_CORE)), HttpStatus.OK);
    }

    @Test
    void anotherStoresStaffCannotReadOrWriteThisStore() {
        String other = tokens.staff(ROLE_STORE_ADMIN, STORE_2);

        expect(privateRead(STORE_1, other), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.PUT, scoped(PRIVATE_STORE, STORE_1), other, storeBody(STORE_1, "Hijack")),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, STORE_1), other, null), HttpStatus.FORBIDDEN);
        expect(upload(scoped(LOGO, STORE_1), other, "x.png"), HttpStatus.FORBIDDEN);
    }

    @Test
    void privateEndpointsRequireAToken() {
        expect(privateRead(STORE_1, null), HttpStatus.UNAUTHORIZED);
        expect(api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, STORE_1), null, null), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void moderatorCanReadButNotChangeTheStore() {
        String moderator = tokens.staff(ROLE_STORE_MODERATOR, STORE_1);

        expect(api.send(HttpMethod.PUT, scoped(SOCIAL_LINKS, STORE_1), moderator, "{\"socialLinks\":[]}"),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, STORE_1), moderator, null), HttpStatus.FORBIDDEN);
    }

    // --------------------------------------------------------------------------------------------- creation

    @Test
    void onlyAStoreCorePrincipalMayCreateAStore() {
        String id = new ObjectId().toHexString();
        String body = storeBody(id, slug("nope"));

        expect(api.send(HttpMethod.POST, PRIVATE_STORE, admin, body), HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, PRIVATE_STORE, null, body), HttpStatus.UNAUTHORIZED);
        expect(api.send(HttpMethod.POST, PRIVATE_STORE, tokens.s2s(SCOPE_STORE_CORE), body), HttpStatus.OK);
        expect(privateRead(id, tokens.s2s(SCOPE_STORE_CORE)), HttpStatus.OK);
    }

    @Test
    void aTakenIdIsAConflictAndAnInvalidBodyIsRejected() {
        ResponseEntity<String> duplicate = api.send(HttpMethod.POST, PRIVATE_STORE, tokens.superAdmin(),
                storeBody(STORE_1, slug("dup")));
        expect(duplicate, HttpStatus.CONFLICT);
        assertThat(json(duplicate).get(CODE).asString()).isEqualTo("MERCHANT.STORE.DUPLICATE");

        expect(api.send(HttpMethod.POST, PRIVATE_STORE, tokens.superAdmin(), "{\"name\":\"no id\"}"),
                HttpStatus.BAD_REQUEST);
    }

    // ------------------------------------------------------------------------------------------- life cycle

    @Test
    void storeLifecycleCreateUpdateUploadDelete() {
        String name = slug("lifecycle");
        String id = createStore(name);
        String owner = tokens.staff(ROLE_STORE_ADMIN, id);

        // created with its name as sub-domain, readable by its own admin and on the storefront
        JsonNode created = json(privateRead(id, owner));
        assertThat(created.get(NAME).asString()).isEqualTo(name);
        assertThat(created.get("org").asString()).isEqualTo(Tokens.ORG_1);
        assertThat(created.get(STORE_DOMAINS_FIELD).get(0).get(DOMAIN_FIELD).asString()).isEqualTo(name);
        assertThat(created.get("address").get("city").asString()).isEqualTo("San Francisco");
        expect(publicRead(id, id), HttpStatus.OK);

        // update keeps identity and the sub-domain
        String renamed = String.format("%s-renamed", name);
        expect(api.send(HttpMethod.PUT, scoped(PRIVATE_STORE, id), owner, storeBody(id, renamed)), HttpStatus.OK);
        JsonNode updated = json(privateRead(id, owner));
        assertThat(updated.get(NAME).asString()).isEqualTo(renamed);
        assertThat(updated.get(STORE_DOMAINS_FIELD).get(0).get(DOMAIN_FIELD).asString()).isEqualTo(name);

        // social links and ordered slider images
        expect(api.send(HttpMethod.PUT, scoped(SOCIAL_LINKS, id), owner,
                "{\"socialLinks\":[{\"provider\":\"INSTAGRAM\",\"url\":\"https://instagram.com/shop\"}]}"),
                HttpStatus.OK);
        expect(api.send(HttpMethod.PUT, scoped(SLIDER_IMAGES, id), owner,
                "{\"sliderImages\":[{\"priority\":0,\"name\":\"first.png\"},{\"priority\":1,\"name\":\"second.png\"}]}"),
                HttpStatus.CREATED);
        JsonNode decorated = json(privateRead(id, owner));
        assertThat(decorated.get("socialLinks").get(0).get("provider").asString()).isEqualTo("INSTAGRAM");
        assertThat(decorated.get(SLIDER_IMAGES_FIELD)).hasSize(2);

        // uploads land in MinIO and are reflected as CDN paths
        expect(upload(scoped(LOGO, id), owner, LOGO_FILE), HttpStatus.CREATED);
        expect(upload(scoped(BANNER, id), owner, "banner.png"), HttpStatus.CREATED);
        ResponseEntity<String> slider = upload(scoped(ADD_SLIDER, id), owner, "slide.png");
        expect(slider, HttpStatus.CREATED);
        JsonNode slide = json(slider);
        assertThat(slide.get("priority").asInt()).isEqualTo(2);
        assertThat(slide.get(NAME).asString()).endsWith(".png");
        assertThat(slide.get("url").asString()).contains(String.format("/files/%s/SLIDER/", id));
        JsonNode withMedia = json(privateRead(id, owner));
        assertThat(withMedia.get(LOGO_FIELD).get(NAME).asString()).isEqualTo(LOGO_FILE);
        assertThat(withMedia.get(BANNER_FIELD).get(PATH_FIELD).asString()).contains(String.format("/files/%s/BANNER/", id));
        assertThat(withMedia.get(SLIDER_IMAGES_FIELD)).hasSize(3);

        // delete, then nothing is left
        expect(api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, id), owner, null), HttpStatus.OK);
        expect(publicRead(id, id), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, id), owner, null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PUT, scoped(PRIVATE_STORE, id), owner, storeBody(id, name)),
                HttpStatus.NOT_FOUND);
    }

    @Test
    void theDefaultStoreCannotBeDeleted() {
        ResponseEntity<String> response = api.send(HttpMethod.DELETE, scoped(PRIVATE_STORE, STORE_1), admin, null);

        expect(response, HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(json(response).get(CODE).asString()).isEqualTo("MERCHANT.STORE.DEFAULT_NOT_REMOVABLE");
        expect(privateRead(STORE_1, admin), HttpStatus.OK);
    }

}
