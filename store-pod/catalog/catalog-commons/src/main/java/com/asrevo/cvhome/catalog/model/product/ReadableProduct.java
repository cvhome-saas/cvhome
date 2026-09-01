package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;

import lombok.Getter;
import lombok.Setter;

/**
 * A product as the listing and the product page read it: the minimal product plus its brand, type and categories.
 * On the product page ({@code GET /product/name/{url}}) it additionally carries the product's option axes and its
 * full variant set; listings leave both empty — a card needs only {@code sku} and {@code variantCount}.
 */
@Getter
@Setter
public class ReadableProduct extends ReadableMinimalProduct {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant creationDate;

    private ReadableManufacturer manufacturer;

    private ReadableProductType type;

    private List<ReadableCategory> categories = new ArrayList<>();

    /**
     * The axes this product varies by, in display order, each carrying only the values its variants actually
     * use. Empty for a simple product.
     */
    private List<ReadableProductOption> options = new ArrayList<>();

    /**
     * Every sellable combination (≥1). Price and stock are merged client-side from the inventory availability
     * call over these skus.
     */
    private List<ReadableProductVariant> variants = new ArrayList<>();
}
