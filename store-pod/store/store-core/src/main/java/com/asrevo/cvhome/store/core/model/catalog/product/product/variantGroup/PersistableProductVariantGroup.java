package com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class PersistableProductVariantGroup extends ProductVariantGroup {

    @Serial
    private static final long serialVersionUID = 1L;

    List<Long> productVariants = null;

}
