package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableProductAvailability implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private StoreMerchantId store;

    private String sku;

    private boolean canBePurchased = false;

    private int quantity = 0;

    private int quantityOrderMaximum = -1; // default unlimited

    private int quantityOrderMinimum = 1; // default 1

    private String dateAvailable;

}
