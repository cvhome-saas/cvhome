package com.asrevo.cvhome.store.core.model.catalog.product.product.variant;

import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProductInventory;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductVariant extends ProductVariant {

    private static final long serialVersionUID = 1L;

    private Long variation;
    private Long variationValue;

    private String variationCode;
    private String variationValueCode;

    private PersistableProductInventory inventory;


}
