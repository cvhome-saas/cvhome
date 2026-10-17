package com.asrevo.cvhome.catalog.model.product;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * The query-string filters of the product listing. Bound by Spring from request parameters, so every field is a
 * plain setter; an absent parameter means "no filter".
 */
@Getter
@Setter
public class ProductFilter {

    /**
     * Substring match on the sku.
     */
    private String sku;

    private Boolean available;

    /**
     * A single category widens to its whole subtree.
     */
    private List<Long> categoryIds;

    private Long manufacturerId;

    /**
     * Option-value ids to filter by: OR within one option, AND across options, anchored to a single variant —
     * "Red and L" means one variant is both.
     */
    private List<Long> optionValueIds;

    /**
     * The filter as one canonical string, for a cache key: every field in a fixed order, lists sorted, so two
     * requests that mean the same listing share an entry and the mutable object itself never sits in a key.
     */
    public String normalised() {
        return String.format("sku=%s;available=%s;categories=%s;manufacturer=%s;options=%s", sku == null ? "" : sku.strip(),
                available, sorted(categoryIds), manufacturerId, sorted(optionValueIds));
    }

    static String sorted(Collection<Long> ids) {
        return ids == null ? "" : ids.stream().filter(Objects::nonNull).sorted().map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
