package com.asrevo.cvhome.inventory.model.price;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductPrice extends Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String originalPrice;

    private String finalPrice;

    private boolean defaultPrice = false;

    private boolean discounted = false;

    private ProductPriceDescription description;

}
