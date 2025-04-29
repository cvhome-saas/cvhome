package com.asrevo.cvhome.catalog.model.product.product.variantGroup;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductVariantGroup extends ProductVariantGroup {

    @Serial private static final long serialVersionUID = 1L;

    List<Long> productVariants = null;
}
