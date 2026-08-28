package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The nullable columns of an image row.
 *
 * <p>
 * {@code isExternal()} guards a specific bug: a {@link ProductImage} built without an image type leaves the column
 * null, and reading it as a raw {@code int} threw a {@link NullPointerException} on the first read of the image.
 * </p>
 */
class ProductImageTest {

    private static final String LIBRARY_PATH = "files/store-1/media/7/shoe.jpg";

    private static final String ALT = "A blue shoe";

    private static final String URL = "https://cdn.example/shoe.jpg";

    @Test
    void anImageWithNoTypeIsALibraryAssetNotAnExternalUrl() {
        ProductImage image = new ProductImage(new Product(), 7L, LIBRARY_PATH, ALT, 2, true);

        assertThat(image.getImageType()).isEqualTo(ProductImage.TYPE_MEDIA_ASSET);
        assertThat(image.isExternal()).isFalse();
        assertThat(image.isDefaultImage()).isTrue();
        assertThat(image.getSortOrder()).isEqualTo(2);
        assertThat(image.getMediaAssetId()).isEqualTo(7L);
        assertThat(image.getAltText()).isEqualTo(ALT);
        assertThat(image.getImageUrl()).isEqualTo(LIBRARY_PATH);
    }

    @Test
    void anExternalImageNeedsBothTheTypeAndTheUrl() {
        ProductImage image = new ProductImage();
        image.setImageType(ProductImage.TYPE_EXTERNAL_URL);

        // the type alone is not enough: without a url there is nothing to serve
        assertThat(image.isExternal()).isFalse();

        image.setProductImageUrl(URL);
        assertThat(image.isExternal()).isTrue();
    }

    @Test
    void unsetFlagsReadAsTheirDefaults() {
        ProductImage image = new ProductImage();

        assertThat(image.isDefaultImage()).isFalse();
        assertThat(image.getSortOrder()).isZero();
        assertThat(image.getImageType()).isEqualTo(ProductImage.TYPE_MEDIA_ASSET);
    }

}
