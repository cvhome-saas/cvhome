package com.asrevo.cvhome.catalog.model.product;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * What a shopper asked the catalogue for. Bound by Spring from request parameters, so every field is a plain
 * setter and an absent parameter means "no filter".
 *
 * <p>
 * Everything here is catalog-owned. Price and stock live in the inventory service, keyed by sku, so they cannot
 * be filtered or sorted on from here — the storefront enriches the page after it comes back.
 * </p>
 */
@Getter
@Setter
public class ProductSearchCriteria {

    /**
     * The raw text the shopper typed. Blank is legitimate: it degrades to a filtered listing, which is what the
     * results page needs when the term is cleared but the filters are kept.
     */
    private String q;

    /**
     * Each category widens to its whole subtree.
     */
    private List<Long> categoryIds;

    private List<Long> manufacturerIds;

    private List<Long> productTypeIds;

    /**
     * Option-value ids: OR within one option, AND across options, anchored to a single variant.
     */
    private List<Long> optionValueIds;

    private Boolean available;

    private ProductSearchSort sort;

    /**
     * Whether to count the facet buckets. Off for the suggest path and for infinite scroll, where the rail is
     * already drawn and the extra aggregate would be wasted.
     */
    private boolean facets = true;

    /**
     * Whether to read the page of products. Off when only the facet rail is wanted, as on the storefront's category
     * page, whose listing comes from {@code /api/v2/products}: the page, its {@code COUNT} and the hydration are then
     * skipped, and {@code content} comes back empty and {@code totalElements} uncounted.
     */
    private boolean rows = true;

    /**
     * Which facet blocks to count; empty counts them all. The category page's rail draws only {@code OPTIONS} (its
     * brands come from the category's own endpoint), and each block left out is a grouped count and a label load saved.
     */
    private Set<SearchFacetGroup> facetGroups;

    public boolean wantsFacets(SearchFacetGroup group) {
        return facets && (facetGroups == null || facetGroups.isEmpty() || facetGroups.contains(group));
    }

    public boolean hasQuery() {
        return q != null && !q.isBlank();
    }

    public String trimmedQuery() {
        return q == null ? "" : q.trim();
    }

    /**
     * The criteria as one canonical string, for a cache key: every field in a fixed order, lists sorted, the query
     * trimmed and lower-cased (the index matches case-insensitively), so two requests that mean the same search
     * share an entry and the mutable object itself never sits in a key.
     */
    public String normalised() {
        String facetKeys = facetGroups == null ? "" : facetGroups.stream().map(Enum::name).sorted()
                .collect(Collectors.joining(","));
        return String.format("q=%s;categories=%s;manufacturers=%s;types=%s;options=%s;available=%s;sort=%s;facets=%s;rows=%s;groups=%s",
                q == null ? "" : q.strip().toLowerCase(Locale.ROOT), ProductFilter.sorted(categoryIds),
                ProductFilter.sorted(manufacturerIds), ProductFilter.sorted(productTypeIds),
                ProductFilter.sorted(optionValueIds), available, sort, facets, rows, facetKeys);
    }
}
