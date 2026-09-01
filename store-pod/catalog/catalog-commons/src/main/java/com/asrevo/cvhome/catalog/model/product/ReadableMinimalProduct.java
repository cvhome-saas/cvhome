package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * The product data every consumer needs — a cart line, a merchandising strip, a search hit. Pure catalog data: price
 * and stock live in the inventory service, keyed by {@code sku}.
 */
@Getter
@Setter
public class ReadableMinimalProduct extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The default variant's sku — or, on a sku-addressed read, the sku that was asked for.
     */
    private String sku;

    /**
     * How many variants the product owns (always at least 1). {@code > 1} is what a card derives "has options"
     * from; the variant rows themselves ship only on the product page shape.
     */
    private int variantCount;

    /**
     * Filled only when this read was addressed by a combination variant's sku: the resolved option/value labels
     * ("Color: Red / Size: L") a cart or order line renders. Null on default variants and on listings.
     */
    private ReadableVariantSelection variant;

    private boolean available;

    private boolean productShipeable;

    private boolean productVirtual;

    private int sortOrder;

    private Instant dateAvailable;

    private ProductSpecification productSpecifications;

    /**
     * The copy in the language asked for.
     */
    private ProductDescription description;

    /**
     * The default image, also present in {@code images}.
     */
    private ReadableImage image;

    private List<ReadableImage> images = new ArrayList<>();
}
