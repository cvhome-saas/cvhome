package com.asrevo.cvhome.store.core.model.catalog.product.inventory;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableInventory extends InventoryEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String creationDate;

    private ReadableMerchantStore store;
    private String sku;
    private List<ReadableProductPrice> prices = new ArrayList<ReadableProductPrice>();
    private String price;


}
