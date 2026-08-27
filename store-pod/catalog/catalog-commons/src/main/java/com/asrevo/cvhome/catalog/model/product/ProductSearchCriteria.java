package com.asrevo.cvhome.catalog.model.product;

import java.util.List;

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

    private Boolean available;

    private ProductSearchSort sort;

    /**
     * Whether to count the facet buckets. Off for the suggest path and for infinite scroll, where the rail is
     * already drawn and the extra aggregate would be wasted.
     */
    private boolean facets = true;

    public boolean hasQuery() {
        return q != null && !q.isBlank();
    }

    public String trimmedQuery() {
        return q == null ? "" : q.trim();
    }
}
