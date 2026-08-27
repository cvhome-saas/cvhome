package com.asrevo.cvhome.catalog.model.product;

/**
 * How a page of search results is ordered.
 *
 * <p>
 * A closed set rather than a free {@code Pageable} sort, because relevance is not a column and the rest have to
 * name columns of {@code Product} — the storefront already documents that anything in a joined table (price, the
 * localized name) cannot be sorted on.
 * </p>
 */
public enum ProductSearchSort {

    /**
     * Cover-density rank of the match. Meaningless without a query, so a blank query falls back to
     * {@link #SORT_ORDER}.
     */
    RELEVANCE,

    NEWEST,

    OLDEST,

    /**
     * The merchant's own ordering.
     */
    SORT_ORDER;

    public static ProductSearchSort orDefault(ProductSearchSort sort) {
        return sort == null ? RELEVANCE : sort;
    }
}
