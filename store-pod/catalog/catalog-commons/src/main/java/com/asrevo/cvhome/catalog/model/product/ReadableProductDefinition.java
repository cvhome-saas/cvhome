package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;
import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * The editable product, as the console's form reads it: every language's copy, and the brand, type and categories
 * as full records so the form can show them without a second call.
 */
@Getter
@Setter
public class ReadableProductDefinition extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String sku;

    /**
     * Same value as {@code sku}; kept because the form was built against it.
     */
    private String identifier;

    private boolean visible;

    private boolean shipeable;

    private boolean virtual;

    private Instant dateAvailable;

    private int sortOrder;

    private ProductSpecification productSpecifications;

    private ReadableProductType type;

    private ReadableManufacturer manufacturer;

    private List<ReadableCategory> categories = new ArrayList<>();

    private ProductDescription description;

    private List<ProductDescription> descriptions = new ArrayList<>();

    private List<ReadableImage> images = new ArrayList<>();
}
