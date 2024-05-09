package com.asrevo.cvhome.store.core.model.catalog.product.variation;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ProductVariationEntity extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;//sku
    private String date;
    private int sortOrder;
    private boolean defaultValue = false;

}
