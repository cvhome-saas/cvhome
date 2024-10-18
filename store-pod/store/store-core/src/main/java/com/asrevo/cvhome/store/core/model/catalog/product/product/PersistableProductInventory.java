package com.asrevo.cvhome.store.core.model.catalog.product.product;

import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableProductInventory implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private String sku;
    private int quantity = 0;
    private PersistableProductPrice price;
}
