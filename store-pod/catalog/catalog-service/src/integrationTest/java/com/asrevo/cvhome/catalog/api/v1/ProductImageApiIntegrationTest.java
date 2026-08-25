package com.asrevo.cvhome.catalog.api.v1;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.asrevo.cvhome.catalog.api.CatalogApiSupport;
import com.asrevo.cvhome.catalog.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.StorageIntegrationTest;
import com.asrevo.cvhome.testsupport.security.TestJwtSigner;

import tools.jackson.databind.JsonNode;

import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.ADMIN;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.CODE;
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
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.query;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.scoped;
import static com.asrevo.cvhome.catalog.api.CatalogApiSupport.slug;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Product images end to end against a real MinIO: the upload writes a file and a row, the reads turn the row into
 * the url a browser fetches, and the deletes take both away.
 *
 * <p>
 * The upload field is {@code file}, not the {@code files} the shared {@code ApiClient} sends, so the multipart
 * request is built here.
 * </p>
 */
@StorageIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class ProductImageApiIntegrationTest {

    private static final String THEIRS = "theirs";

    private static final String PRODUCT = "product";

    private static final String IMAGE = "image";

    private static final String IMAGES = "images";

    private static final String IMAGE_NAME = "imageName";

    private static final String IMAGE_URL = "imageUrl";

    private static final String DEFAULT_IMAGE = "defaultImage";

    private static final String ORDER = "order";

    private static final String FILE_FIELD = "file";

    private static final String PNG = "%s.png";

    private static final String ORDER_QUERY = "order=%d";

    /** Seeded product of store A: three images, the first of them the default. */
    private static final long SEEDED_PRODUCT = 1L;

    private static final String PRODUCT_BODY = """
            {"sku":"%s","visible":true,"shipeable":true,
             "descriptions":[{"language":"en","name":"Imaged","friendlyUrl":"%s"}]}""";

    @LocalServerPort
    private int port;

    @Autowired
    private TestJwtSigner signer;

    private CatalogApiSupport api;

    private RestClient http;

    private String admin;

    @BeforeEach
    void setUp() {
        api = new CatalogApiSupport(port, signer);
        http = RestClient.builder().baseUrl(String.format("http://localhost:%d", port))
                .defaultStatusHandler(s -> true, (request, response) -> { })
                .build();
        admin = api.token(ADMIN, STORE_A);
    }

    // ------------------------------------------------------------------------------------------------- helpers

    private static byte[] png() {
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.CYAN);
        graphics.fillRect(0, 0, 24, 24);
        graphics.dispose();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ResponseEntity<String> upload(String url, String token, String filename, byte[] bytes) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add(FILE_FIELD, new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        RestClient.RequestBodySpec spec = http.post().uri(url).contentType(MediaType.MULTIPART_FORM_DATA);
        if (token != null) {
            spec = spec.header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token));
        }
        return spec.body(form).retrieve().toEntity(String.class);
    }

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

    private static String uploadUrl(long productId, String store) {
        return scoped(path(V1_PRIVATE, PRODUCT, productId, IMAGE), store);
    }

    private static String imageUrl(long productId, long imageId, String store) {
        return scoped(path(V1_PRIVATE, PRODUCT, productId, IMAGE, imageId), store);
    }

    // ------------------------------------------------------------------------------------------------- reads

    @Test
    void theSeededProductAnswersItsImagesInDisplayOrder() {
        JsonNode images = images(SEEDED_PRODUCT, STORE_A);
        assertThat(images).hasSizeGreaterThanOrEqualTo(3);
        // the list is sorted by sort order, and the url is the CDN path under the product's sku
        int previous = -1;
        for (JsonNode image : images) {
            assertThat(image.get(ORDER).asInt()).isGreaterThanOrEqualTo(previous);
            previous = image.get(ORDER).asInt();
            assertThat(image.get(IMAGE_URL).asString()).contains(image.get(IMAGE_NAME).asString());
        }
        assertThat(images.valueStream().filter(i -> i.get(DEFAULT_IMAGE).asBoolean()).count()).isEqualTo(1);

        expect(api.get(scoped(path(V1, PRODUCT, 999999L, IMAGES), STORE_A), null), HttpStatus.NOT_FOUND);
    }

    // ------------------------------------------------------------------------------------------------- writes

    @Test
    void imageLifecycleUploadReorderDelete() {
        long product = createProduct();
        assertThat(images(product, STORE_A)).isEmpty();

        // the first image of a product becomes its default even when the caller does not ask for one
        expect(upload(uploadUrl(product, STORE_A), admin, String.format(PNG, slug("first")), png()),
                HttpStatus.CREATED);
        JsonNode first = images(product, STORE_A);
        assertThat(first).hasSize(1);
        assertThat(first.get(0).get(DEFAULT_IMAGE).asBoolean()).isTrue();
        assertThat(first.get(0).get(IMAGE_URL).asString()).contains(first.get(0).get(IMAGE_NAME).asString());

        // a second upload does not steal the default
        expect(upload(query(uploadUrl(product, STORE_A), String.format(ORDER_QUERY, 5)), admin,
                String.format(PNG, slug("second")), png()), HttpStatus.CREATED);
        JsonNode both = images(product, STORE_A);
        assertThat(both).hasSize(2);
        assertThat(both.valueStream().filter(i -> i.get(DEFAULT_IMAGE).asBoolean()).count()).isEqualTo(1);
        assertThat(both.get(1).get(ORDER).asInt()).isEqualTo(5);

        // reorder moves it back to the front of the list
        long secondId = both.get(1).get(ID).asLong();
        expect(api.send(HttpMethod.PATCH,
                query(imageUrl(product, secondId, STORE_A), String.format(ORDER_QUERY, -1)), admin, null),
                HttpStatus.OK);
        assertThat(images(product, STORE_A).get(0).get(ID).asLong()).isEqualTo(secondId);

        expect(api.send(HttpMethod.DELETE, imageUrl(product, secondId, STORE_A), admin, null), HttpStatus.OK);
        assertThat(images(product, STORE_A)).hasSize(1);
        // the row is gone, so the same delete twice is a 404 rather than a silent success
        var gone = api.send(HttpMethod.DELETE, imageUrl(product, secondId, STORE_A), admin, null);
        expect(gone, HttpStatus.NOT_FOUND);
        assertThat(json(gone).get(CODE).asString()).isEqualTo("CATALOG.PRODUCT_IMAGE.NOT_FOUND");

        // deleting the product takes its remaining images with it
        expect(api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, product), STORE_A), admin, null),
                HttpStatus.OK);
        expect(api.get(scoped(path(V1, PRODUCT, product, IMAGES), STORE_A), null), HttpStatus.NOT_FOUND);
    }

    @Test
    void anEmptyPartIsSkippedAndAnUnknownProductIsRefused() {
        long product = createProduct();
        // MultipartFile.isEmpty() parts are skipped rather than written as zero-byte objects
        expect(upload(uploadUrl(product, STORE_A), admin, String.format(PNG, slug("empty")), new byte[0]),
                HttpStatus.CREATED);
        assertThat(images(product, STORE_A)).isEmpty();

        expect(upload(uploadUrl(999999L, STORE_A), admin, String.format(PNG, slug("nowhere")), png()),
                HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, product), STORE_A), admin, null),
                HttpStatus.OK);
    }

    // ------------------------------------------------------------------------------------ tenancy + permissions

    @Test
    void anotherStoreCannotUploadToOrRemoveThisStoresImages() {
        long product = createProduct();
        expect(upload(uploadUrl(product, STORE_A), admin, String.format(PNG, slug("mine")), png()),
                HttpStatus.CREATED);
        long imageId = images(product, STORE_A).get(0).get(ID).asLong();
        String other = api.token(ADMIN, STORE_B);

        expect(upload(uploadUrl(product, STORE_A), other, String.format(PNG, slug(THEIRS)), png()),
                HttpStatus.FORBIDDEN);
        expect(upload(uploadUrl(product, STORE_B), other, String.format(PNG, slug(THEIRS)), png()),
                HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.DELETE, imageUrl(product, imageId, STORE_B), other, null), HttpStatus.NOT_FOUND);
        expect(api.send(HttpMethod.PATCH, imageUrl(product, imageId, STORE_B), other, null), HttpStatus.NOT_FOUND);
        // the public list of a store-A product is empty when asked for under store B
        expect(api.get(scoped(path(V1, PRODUCT, product, IMAGES), STORE_B), null), HttpStatus.NOT_FOUND);

        expect(api.send(HttpMethod.DELETE, scoped(path(V1_PRIVATE, PRODUCT, product), STORE_A), admin, null),
                HttpStatus.OK);
    }

    @Test
    void aModeratorAndAnAnonymousCallerCannotWriteImages() {
        String moderator = api.token(MODERATOR, STORE_A);
        expect(upload(uploadUrl(SEEDED_PRODUCT, STORE_A), moderator, String.format(PNG, slug("mod")), png()),
                HttpStatus.FORBIDDEN);
        expect(api.send(HttpMethod.DELETE, imageUrl(SEEDED_PRODUCT, 1L, STORE_A), moderator, null),
                HttpStatus.FORBIDDEN);
        expect(upload(uploadUrl(SEEDED_PRODUCT, STORE_A), null, String.format(PNG, slug("anon")), png()),
                HttpStatus.UNAUTHORIZED);
        // the storefront list stays open
        expect(api.get(scoped(path(V1, PRODUCT, SEEDED_PRODUCT, IMAGES), STORE_A), null), HttpStatus.OK);
    }

}
