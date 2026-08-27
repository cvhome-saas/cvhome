package com.asrevo.cvhome.catalog.entity;

import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The composite key is what tells one product's English document from its Arabic one. Hibernate leans on its
 * equality to decide whether two rows are the same row, so getting it wrong would silently collapse a
 * product's languages into one.
 */
class ProductSearchIndexIdTest {

    private static final String EN = "en";

    private static final String AR = "ar";

    @Test
    void sameProductAndLanguageIsTheSameRow() {
        assertThat(new ProductSearchIndexId(1L, EN)).isEqualTo(new ProductSearchIndexId(1L, EN))
                .hasSameHashCodeAs(new ProductSearchIndexId(1L, EN));
    }

    @Test
    void oneProductsLanguagesAreDifferentRows() {
        assertThat(new ProductSearchIndexId(1L, EN)).isNotEqualTo(new ProductSearchIndexId(1L, AR));
        assertThat(Set.of(new ProductSearchIndexId(1L, EN), new ProductSearchIndexId(1L, AR))).hasSize(2);
    }

    @Test
    void theSameLanguageAcrossProductsAreDifferentRows() {
        assertThat(new ProductSearchIndexId(1L, EN)).isNotEqualTo(new ProductSearchIndexId(2L, EN));
    }

    @Test
    void comparesSafelyAgainstNullAndOtherTypes() {
        ProductSearchIndexId id = new ProductSearchIndexId(1L, EN);

        assertThat(id).isEqualTo(id).isNotEqualTo(null).isNotEqualTo("1:en");
    }

    @Test
    void theNoArgsConstructorHibernateNeedsExists() {
        ProductSearchIndexId id = new ProductSearchIndexId();
        id.setProductId(1L);
        id.setLanguageCode(EN);

        assertThat(id).isEqualTo(new ProductSearchIndexId(1L, EN));
    }
}
