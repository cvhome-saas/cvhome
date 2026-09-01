package com.asrevo.cvhome.catalog.model.product;

import java.util.List;

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
}
