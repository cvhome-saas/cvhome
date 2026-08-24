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

    private String sku;

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
