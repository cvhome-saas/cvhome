package com.asrevo.cvhome.catalog.api.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.content.api.ExternalMediaService;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ID;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.MODERATOR;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_A;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.STORE_B;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V1_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.V2_PRIVATE;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.expect;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.json;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.path;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

/**
 * A product's gallery end to end: attaching library assets, replacing the whole gallery, and detaching.
 *
 * <p>
 * Catalog no longer stores files, so there is no MinIO in this story any more — the bytes are the media library's
 * problem. What is tested here is the reference: that an asset id which is not this store's is refused, that the
 * asset's path is cached at attach time and served under this environment's CDN, and that the seller can finally
 * choose which image is the default.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductImageApiIntegrationTest {

    private static final String PRODUCT = "product";

    private static final String IMAGE = "image";

    private static final String IMAGES = "images";

    private static final String IMAGE_URL = "imageUrl";

    private static final String MEDIA_ASSET_ID = "mediaAssetId";

    private static final String DEFAULT_IMAGE = "defaultImage";

    private static final String ALT_TEXT = "altText";

    private static final String ORDER = "order";

    /** Seeded product of store A: three images, the first of them the default. */
    private static final long SEEDED_PRODUCT = 1L;

    private static final String PRODUCT_BODY = """
            {"sku":"%s","visible":true,"shipeable":true,
             "descriptions":[{"language":"en","name":"Imaged","friendlyUrl":"%s"}]}""";

    private static final String ATTACH_BODY = """
            [{"mediaAssetId":%d,"altText":"%s"}]""";

    private static final String ALT = "A cyan square";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    @Autowired
    private ExternalMediaService media;

    @Value("${com.asrevo.cvhome.cdn.base-path}")
    private String cdnBasePath;

    private CatalogApiSupport api;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new CatalogApiSupport(port, signer);
        admin = api.token(ADMIN, STORE_A);
        // The media client is shared by every catalog integration test, so each one starts from the default
        // answer rather than whatever the last test stubbed.
        org.mockito.Mockito.reset(media);
        ExternalClientsTestConfiguration.stubMediaDefaults(media);
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private long createProduct() {
        String sku = slug("SKU-IMG").toUpperCase();
        var created = api.send(HttpMethod.POST, scoped(path(V2_PRIVATE, PRODUCT), STORE_A), admin,
                String.format(PRODUCT_BODY, sku, sku.toLowerCase()));
        expect(created, HttpStatus.CREATED);
        return json(created).get(ID).asLong();
    }

    private JsonNode images(long productId, String store) {
        var response = api.get(scoped(path(V1, PRODUCT, productId, IMAGES), store), null);
        expect(response, HttpStatus.OK);
        return json(response);
    }

    private static String galleryUrl(long productId, String store) {
        return scoped(path(V1_PRIVATE, PRODUCT, productId, IMAGES), store);
    }

    private static String imageUrl(long productId, long imageId, String store) {
        return scoped(path(V1_PRIVATE, PRODUCT, productId, IMAGE, imageId), store);
    }

    // ------------------------------------------------------------------------------------------------- reads

    @Test
    void theSeededProductAnswersItsImagesInDisplayOrder() {
        JsonNode images = images(SEEDED_PRODUCT, STORE_A);

        assertThat(images.size()).isGreaterThanOrEqualTo(1);
        assertThat(images.get(0).get(ORDER).asInt()).isZero();
        // The seeded photos are registered library assets, so they carry an id and a cached path.
        assertThat(images.get(0).get(MEDIA_ASSET_ID).isNull()).isFalse();
        // The seed stores a path and nothing about the host, so this is the CDN this environment configures —
        // here the MinIO container, an address no script could have written down.
        assertThat(images.get(0).get(IMAGE_URL).asString()).startsWith(cdnBasePath);
    }

    // ------------------------------------------------------------------------------------------------- writes

    @Test
    void galleryLifecycleAttachReplaceDetach() {
        long product = createProduct();

        var attached = api.send(HttpMethod.POST, galleryUrl(product, STORE_A), admin,
                String.format(ATTACH_BODY, 11L, ALT));
        expect(attached, HttpStatus.CREATED);
        JsonNode after = json(attached);
        assertThat(after).hasSize(1);
        assertThat(after.get(0).get(MEDIA_ASSET_ID).asLong()).isEqualTo(11L);
        assertThat(after.get(0).get(ALT_TEXT).asString()).isEqualTo(ALT);
        // The path is cached when it is attached, so reading a product never calls content; the url is that
        // path under this environment's CDN, composed on the way out.
        assertThat(after.get(0).get(IMAGE_URL).asString())
                .startsWith(cdnBasePath)
                .endsWith("/media/11/asset.png");
        // The first image of an empty gallery is the default without being asked.
        assertThat(after.get(0).get(DEFAULT_IMAGE).asBoolean()).isTrue();

        // Replace: order is the list order, and the flagged item wins. The old API could only renumber, so the
        // default image was visible in the console but not changeable.
        var replaced = api.send(HttpMethod.PUT, galleryUrl(product, STORE_A), admin,
                """
                [{"mediaAssetId":22},{"mediaAssetId":11,"defaultImage":true}]""");
        expect(replaced, HttpStatus.OK);
        JsonNode gallery = json(replaced);
        assertThat(gallery).hasSize(2);
        assertThat(gallery.get(0).get(MEDIA_ASSET_ID).asLong()).isEqualTo(22L);
        assertThat(gallery.get(0).get(DEFAULT_IMAGE).asBoolean()).isFalse();
        assertThat(gallery.get(1).get(DEFAULT_IMAGE).asBoolean()).isTrue();

        long first = gallery.get(0).get(ID).asLong();
        expect(api.send(HttpMethod.DELETE, imageUrl(product, first, STORE_A), admin, null),
                HttpStatus.NO_CONTENT);
        assertThat(images(product, STORE_A)).hasSize(1);
    }

    /**
     * Content answers by omitting ids that are not this store's, which is what catches an asset borrowed from
     * another seller. Nothing is written when it does.
     */
    @Test
    void anAssetTheLibraryDoesNotOwnIsRefused() {
        long product = createProduct();
        // doReturn, not when(...): when() calls the mock, which would run the default answer with null args.
        org.mockito.Mockito.doReturn(java.util.List.of()).when(media).resolve(any(), any());

        var refused = api.send(HttpMethod.POST, galleryUrl(product, STORE_A), admin,
                String.format(ATTACH_BODY, 99L, ALT));
        expect(refused, HttpStatus.BAD_REQUEST);
        assertThat(refused.getBody()).contains("CATALOG.PRODUCT_IMAGE.ASSET_UNKNOWN");
        assertThat(images(product, STORE_A)).isEmpty();
    }

    @Test
    void anUnknownProductIsRefused() {
        expect(api.send(HttpMethod.POST, galleryUrl(999999L, STORE_A), admin,
                String.format(ATTACH_BODY, 11L, ALT)), HttpStatus.NOT_FOUND);
    }

    @Test
    void anotherStoreCannotTouchThisStoresGallery() {
        long product = createProduct();
        expect(api.send(HttpMethod.POST, galleryUrl(product, STORE_A), admin,
                String.format(ATTACH_BODY, 11L, ALT)), HttpStatus.CREATED);
        long imageId = images(product, STORE_A).get(0).get(ID).asLong();
        String other = api.token(ADMIN, STORE_B);

        expect(api.send(HttpMethod.POST, galleryUrl(product, STORE_B), other,
                String.format(ATTACH_BODY, 11L, ALT)), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, imageUrl(product, imageId, STORE_B), other, null),
                HttpStatus.NOT_FOUND);
        assertThat(images(product, STORE_A)).hasSize(1);
    }

    @Test
    void aModeratorAndAnAnonymousCallerCannotWriteImages() {
        long product = createProduct();
        String body = String.format(ATTACH_BODY, 11L, ALT);

        expect(api.send(HttpMethod.POST, galleryUrl(product, STORE_A), api.token(MODERATOR, STORE_A), body),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.POST, galleryUrl(product, STORE_A), null, body), HttpStatus.UNAUTHORIZED);
    }

}
