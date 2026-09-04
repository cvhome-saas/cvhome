package com.asrevo.cvhome.catalog.repositories;

import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.domain.Specification;

import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The catalog's search predicates, and which of them mean "no filter".
 *
 * <p>
 * Every optional filter has to answer {@link ProductSpecifications#always()} for an absent value — a null that
 * became {@code equal(field, null)} instead would match nothing and empty the catalogue. That distinction is
 * invisible in a browser until a facet nobody selected silently hides every product, so each filter is asserted
 * both ways.
 * </p>
 *
 * <p>
 * Driven through a mocked {@link CriteriaBuilder} rather than a database: what is being asserted is which
 * predicates get built and which are skipped, not what Postgres does with them. The full-text functions
 * ({@code fts_match}, {@code fts_rank}) are Postgres's and are exercised by
 * {@code ProductSearchApiIntegrationTest}.
 * </p>
 */
class ProductSpecificationsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final LanguageCode ENGLISH = new LanguageCode("en");
    private static final String TEXT = "shoes";

    private CriteriaBuilder builder;
    private CriteriaQuery<?> query;
    private Root<Product> root;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        builder = Mockito.mock(CriteriaBuilder.class, Mockito.RETURNS_DEEP_STUBS);
        query = Mockito.mock(CriteriaQuery.class, Mockito.RETURNS_DEEP_STUBS);
        root = Mockito.mock(Root.class, Mockito.RETURNS_DEEP_STUBS);
        when(query.subquery(any(Class.class))).thenReturn(Mockito.mock(Subquery.class, Mockito.RETURNS_DEEP_STUBS));
    }

    private Object build(Specification<Product> specification) {
        return specification.toPredicate(root, query, builder);
    }

    @Test
    void anAbsentFilterIsNoPredicateRatherThanAPredicateMatchingNothing() {
        // The whole point: null must not become equal(field, null), which empties the catalogue.
        assertThat(build(ProductSpecifications.always())).isNull();
        assertThat(build(ProductSpecifications.available(null))).isNull();
        assertThat(build(ProductSpecifications.skuLike(null))).isNull();
        assertThat(build(ProductSpecifications.skuLike("   "))).isNull();
        assertThat(build(ProductSpecifications.hasOptionValues(null))).isNull();
        assertThat(build(ProductSpecifications.hasOptionValues(Map.of()))).isNull();
        assertThat(build(ProductSpecifications.inCategories(null))).isNull();
        assertThat(build(ProductSpecifications.inCategories(List.of()))).isNull();
        assertThat(build(ProductSpecifications.byManufacturers(null))).isNull();
        assertThat(build(ProductSpecifications.byManufacturers(List.of()))).isNull();
        assertThat(build(ProductSpecifications.byTypes(null))).isNull();
        assertThat(build(ProductSpecifications.byTypes(List.of()))).isNull();
        assertThat(build(ProductSpecifications.matchesText(null, STORE, ENGLISH))).isNull();
        assertThat(build(ProductSpecifications.matchesText("  ", STORE, ENGLISH))).isNull();
    }

    @Test
    void theStorePredicateIsAPlainEqualityOnTheStoreColumn() {
        build(ProductSpecifications.inStore(STORE));

        verify(root).get("store");
        verify(builder).equal(any(), eq(STORE));
    }

    @Test
    void availabilityIsFilteredWhenItIsAskedForEitherWay() {
        build(ProductSpecifications.available(true));
        build(ProductSpecifications.available(false));

        verify(root, Mockito.times(2)).get("available");
        verify(builder).equal(any(), eq(true));
        verify(builder).equal(any(), eq(false));
    }

    @Test
    void aSkuSearchIsACaseInsensitiveContainsAgainstTheVariantsSku() {
        build(ProductSpecifications.skuLike("ABC"));

        // Lower-cased on both sides, so "ABC" finds "abc-1".
        verify(builder).like(any(), eq("%abc%"));
        verify(builder).exists(any());
    }

    @Test
    void theManufacturerAndTypeFiltersAreInClausesOnTheirJoinedId() {
        build(ProductSpecifications.byManufacturers(List.of(1L, 2L)));
        build(ProductSpecifications.byTypes(List.of(3L)));

        verify(root).get("manufacturer");
        verify(root).get("type");
    }

    @Test
    void aCategoryFilterCorrelatesBackToTheProductRatherThanJoiningItTwice() {
        build(ProductSpecifications.inCategories(List.of(1L)));

        verify(builder).exists(any());
    }

    @Test
    void eachChosenOptionAddsItsOwnExistsSoTheFiltersAndTogether() {
        build(ProductSpecifications.hasOptionValues(Map.of(1L, List.of(10L, 11L), 2L, List.of(20L))));

        // One exists for the variant, plus one per option: a variant has to satisfy every chosen option, not any.
        verify(builder, Mockito.atLeast(3)).exists(any());
    }

    @Test
    void aTextSearchIsScopedToTheStoreAndTheLanguageAsWellAsTheProduct() {
        build(ProductSpecifications.matchesText(TEXT, STORE, ENGLISH));

        // Without the store predicate the index would match another merchant's product text.
        verify(builder).equal(any(), eq(STORE));
        verify(builder).equal(any(), eq(ENGLISH.code()));
        verify(builder).function(eq("fts_match"), eq(Boolean.class), any(), any(), any());
    }

    @Test
    void relevanceRanksAgainstTheSameStoreAndLanguageScopedIndex() {
        ProductSpecifications.relevance(root, query, builder, TEXT, STORE, ENGLISH);

        verify(builder).function(eq("fts_rank"), eq(Float.class), any(), any(), any());
        verify(builder).equal(any(), eq(STORE));
    }

    @Test
    void theQueryTextTravelsAsABoundLiteralRatherThanBeingSpelledIntoTheFunctionCall() {
        build(ProductSpecifications.matchesText(TEXT, STORE, ENGLISH));

        verify(builder).literal(TEXT);
    }
}
