package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;

import lombok.Getter;
import lombok.Setter;

/**
 * A product as the listing and the product page read it: the minimal product plus its brand, type and categories.
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
}
