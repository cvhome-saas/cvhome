package com.asrevo.cvhome.catalog.model.product.product.definition;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.category.Category;
import com.asrevo.cvhome.catalog.model.product.ProductDescription;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductDefinition extends ProductDefinition {

    /**
     * type and manufacturer are String type corresponding to the unique code
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductDescription> descriptions = new ArrayList<>();

    private List<PersistableProductAttribute> properties = new ArrayList<>();

    private List<Category> categories = new ArrayList<>();

    private String type;

    private String manufacturer;

    private BigDecimal price;

    private int quantity;

}
