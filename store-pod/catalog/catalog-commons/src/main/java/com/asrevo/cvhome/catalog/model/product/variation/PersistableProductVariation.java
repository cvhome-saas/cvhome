package com.asrevo.cvhome.catalog.model.product.variation;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * A Variant
 *
 * @author carlsamson
 */
@Setter
@Getter
public class PersistableProductVariation extends ProductVariationEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Long option = null;
    private Long optionValue = null;
}
