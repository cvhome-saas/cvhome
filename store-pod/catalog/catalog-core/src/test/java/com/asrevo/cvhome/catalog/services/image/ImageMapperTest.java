package com.asrevo.cvhome.catalog.services.image;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.entity.ProductImage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Where a product image is fetched from.
 *
 * <p>
 * The row stores the asset's path in the bucket and nothing about the host, so the same row serves a local MinIO,
 * a second local stack on a shifted port, and a CDN, purely from configuration. Storing the whole url instead is
 * what shipped a demo catalogue every environment served from one developer's machine.
 * </p>
 */
class ImageMapperTest {

    private static final String BASE = "https://cdn.example/bucket";

    private static final String PATH = "products/store-1/SKU-1/SMALL/shoe.jpg";

    private static final String RESOLVED = "https://cdn.example/bucket/products/store-1/SKU-1/SMALL/shoe.jpg";

    private static final String EXTERNAL = "https://youtu.be/xyz";

    @Test
    void aLibraryImageIsServedFromTheConfiguredCdn() {
        assertThat(new ImageMapper(BASE).url(library(PATH))).isEqualTo(RESOLVED);
    }

    /** A base with a trailing slash is the same base; it must not produce a doubled separator. */
    @Test
    void theBaseIsJoinedWithExactlyOneSlash() {
        assertThat(new ImageMapper("https://cdn.example/bucket/").url(library(PATH))).isEqualTo(RESOLVED);
    }

    /** An external row serves its own url; the library column is not consulted, so the two cannot disagree. */
    @Test
    void anExternalImageIsServedFromWhereverItLives() {
        ProductImage image = library(PATH);
        image.setImageType(ProductImage.TYPE_EXTERNAL_URL);
        image.setProductImageUrl(EXTERNAL);

        assertThat(new ImageMapper(BASE).url(image)).isEqualTo(EXTERNAL);
    }

    /** Rows written before the column held a path carry a whole url. Re-hosting them would only guess. */
    @Test
    void aStoredUrlIsLeftAlone() {
        assertThat(new ImageMapper(BASE).url(library(EXTERNAL))).isEqualTo(EXTERNAL);
    }

    @Test
    void withNoCdnConfiguredThePathIsServedAsItStands() {
        assertThat(new ImageMapper("").url(library(PATH))).isEqualTo(PATH);
        assertThat(new ImageMapper(null).url(library(PATH))).isEqualTo(PATH);
    }

    @Test
    void anImageWithNothingStoredHasNoUrl() {
        assertThat(new ImageMapper(BASE).url(library(null))).isNull();
    }

    private static ProductImage library(String path) {
        return new ProductImage(new Product(), 7L, path, null, 0, false);
    }

}
