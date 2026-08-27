package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The filter rail for a set of results: what could still be narrowed, and by how much.
 *
 * <p>
 * Price and stock are absent on purpose — they live in the inventory service, keyed by sku, so the catalogue
 * cannot count them without a cross-service call on the hot path.
 * </p>
 */
@Getter
@Setter
public class ReadableSearchFacets implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableFacetBucket> categories = new ArrayList<>();

    private List<ReadableFacetBucket> brands = new ArrayList<>();

    private List<ReadableFacetBucket> types = new ArrayList<>();
}
