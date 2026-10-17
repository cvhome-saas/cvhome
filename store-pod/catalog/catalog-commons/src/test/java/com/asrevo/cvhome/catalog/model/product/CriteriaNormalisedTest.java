package com.asrevo.cvhome.catalog.model.product;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Two requests that mean the same listing or search render to the same canonical string, whatever the order of
 * their ids or the case and spacing of the typed text; the string is what a cache key hashes.
 */
class CriteriaNormalisedTest {

    @Test
    void aFilterRendersItsFieldsInAFixedOrderWithSortedIds() {
        ProductFilter first = new ProductFilter();
        first.setSku(" SKU-1 ");
        first.setCategoryIds(List.of(3L, 1L, 2L));
        first.setManufacturerId(9L);
        ProductFilter second = new ProductFilter();
        second.setSku("SKU-1");
        second.setCategoryIds(List.of(1L, 2L, 3L));
        second.setManufacturerId(9L);

        assertThat(first.normalised()).isEqualTo(second.normalised())
                .isEqualTo("sku=SKU-1;available=null;categories=1,2,3;manufacturer=9;options=");
        assertThat(new ProductFilter().normalised()).isEqualTo("sku=;available=null;categories=;manufacturer=null;options=");
    }

    @Test
    void aSearchRendersTheQueryLoweredAndTrimmedAndItsSetsSorted() {
        ProductSearchCriteria first = new ProductSearchCriteria();
        first.setQ("  Running SHOES ");
        first.setManufacturerIds(List.of(5L, 4L));
        first.setFacetGroups(Set.of(SearchFacetGroup.values()));
        first.setRows(false);
        ProductSearchCriteria second = new ProductSearchCriteria();
        second.setQ("running shoes");
        second.setManufacturerIds(List.of(4L, 5L));
        second.setFacetGroups(Set.of(SearchFacetGroup.values()));
        second.setRows(false);

        assertThat(first.normalised()).isEqualTo(second.normalised()).startsWith("q=running shoes;categories=;manufacturers=4,5;")
                .contains(";rows=false;");
        assertThat(new ProductSearchCriteria().normalised()).startsWith("q=;").endsWith(";facets=true;rows=true;groups=");
    }
}
