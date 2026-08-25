package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The nullable columns of an image row.
 *
 * <p>
 * {@code isExternal()} guards a specific bug: the upload path builds a {@link ProductImage} without an image type,
 * so the column is null, and reading it as a raw {@code int} threw a {@link NullPointerException} on the first read
 * of every freshly uploaded image.
 * </p>
 */
class ProductImageTest {

    private static final String FILE = "shoe.jpg";

    private static final String URL = "https://cdn.example/shoe.jpg";

    @Test
    void anImageWithNoTypeIsAFileNotAnExternalUrl() {
        ProductImage image = new ProductImage(new Product(), FILE, 2, true);

        assertThat(image.getImageType()).isEqualTo(ProductImage.TYPE_FILE);
        assertThat(image.isExternal()).isFalse();
        assertThat(image.isDefaultImage()).isTrue();
        assertThat(image.getSortOrder()).isEqualTo(2);
        assertThat(image.getProductImage()).isEqualTo(FILE);
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
        assertThat(image.getImageType()).isEqualTo(ProductImage.TYPE_FILE);
    }

}
