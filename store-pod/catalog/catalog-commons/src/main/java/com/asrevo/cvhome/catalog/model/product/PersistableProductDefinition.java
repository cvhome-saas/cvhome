package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.catalog.model.category.CategoryReference;
import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * The product as the console writes it. {@code type} and {@code manufacturer} are the entities' unique codes;
 * {@code categories} are references by id or code. Price and stock are written to the inventory service.
 */
@Getter
@Setter
public class PersistableProductDefinition extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    private String sku;

    private boolean visible = true;

    private boolean shipeable = true;

    private boolean virtual;

    private Instant dateAvailable;

    private int sortOrder;

    private ProductSpecification productSpecifications;

    private String type;

    private String manufacturer;

    private List<CategoryReference> categories = new ArrayList<>();

    private List<ProductDescription> descriptions = new ArrayList<>();
}
