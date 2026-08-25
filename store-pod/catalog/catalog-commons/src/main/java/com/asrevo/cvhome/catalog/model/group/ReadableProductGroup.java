package com.asrevo.cvhome.catalog.model.group;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * A named set of products — a merchandising strip such as {@code FEATURED_ITEMS}, or, when {@code parentProduct}
 * is set, the products related to that one product.
 */
@Getter
@Setter
public class ReadableProductGroup extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private boolean active;

    private ProductGroupDescription description;

    private List<ProductGroupDescription> descriptions = new ArrayList<>();

    private ReadableMinimalProduct parentProduct;

    private List<ReadableMinimalProduct> products = new ArrayList<>();
}
