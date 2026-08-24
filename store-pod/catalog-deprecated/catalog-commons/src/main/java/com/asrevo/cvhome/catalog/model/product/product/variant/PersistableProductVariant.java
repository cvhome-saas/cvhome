package com.asrevo.cvhome.catalog.model.product.product.variant;

import java.io.Serial;

import com.asrevo.cvhome.catalog.model.product.product.PersistableProductInventory;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductVariant extends ProductVariant {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long variation;

    private Long variationValue;

    private String variationCode;

    private String variationValueCode;

    private PersistableProductInventory inventory;

}
