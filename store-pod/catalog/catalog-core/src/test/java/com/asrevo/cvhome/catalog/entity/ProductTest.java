package com.asrevo.cvhome.catalog.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.LanguageCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The product's own decisions: which copy answers a language, which image is the default, and what its nullable
 * flags mean when the column has never been written.
 */
class ProductTest {

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String SHOE = "Shoe";

    private static ProductDescription copy(Product product, LanguageCode language, String name) {
        ProductDescription description = new ProductDescription(product);
        description.setLanguageCode(language);
        description.setName(name);
        product.getDescriptions().add(description);
        return description;
    }

    @Test
    void copyIsAnsweredPerLanguageAndNeverSubstituted() {
        Product product = new Product();
        copy(product, EN, SHOE);
        copy(product, AR, "حذاء");

        assertThat(product.description(EN)).get().extracting(ProductDescription::getName).isEqualTo(SHOE);
        assertThat(product.description(AR)).isPresent();
        // a language the product has no copy in answers nothing rather than falling back to another one
        assertThat(product.description(FR)).isEmpty();
    }

    @Test
    void theFlaggedImageWinsAndOtherwiseTheLowestSortOrderDoes() {
        Product product = new Product();
        ProductImage second = new ProductImage(product, "b.jpg", 2, false);
        ProductImage first = new ProductImage(product, "a.jpg", 1, false);
        product.getImages().add(second);
        product.getImages().add(first);

        assertThat(product.defaultImage()).contains(first);

        ProductImage flagged = new ProductImage(product, "c.jpg", 9, true);
        product.getImages().add(flagged);
        assertThat(product.defaultImage()).contains(flagged);
    }

    @Test
    void aProductWithNoImagesHasNoDefault() {
        assertThat(new Product().defaultImage()).isEmpty();
    }

    @Test
    void anUnsetAvailableFlagMeansVisible() {
        // The column predates the merchandising switch, so old rows hold null; treating that as "hidden" would
        // empty out a migrated catalog.
        Product product = new Product();

        assertThat(product.isAvailable()).isTrue();
        assertThat(product.isProductVirtual()).isFalse();
        assertThat(product.isProductShipeable()).isFalse();
        assertThat(product.getSortOrder()).isZero();

        product.setAvailable(false);
        assertThat(product.isAvailable()).isFalse();
    }

}
