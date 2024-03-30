package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ReadableProductPrice extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String originalPrice;
    private String finalPrice;
    private boolean defaultPrice = false;
    private boolean discounted = false;
    private ProductPriceDescription description;

}
