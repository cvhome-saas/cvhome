package com.asrevo.cvhome.store.core.model.catalog.product.variation;

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
    private static final long serialVersionUID = 1L;
    private Long option = null;
    private Long optionValue = null;


}
