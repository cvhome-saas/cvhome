package com.asrevo.cvhome.store.core.model.catalog.product.inventory;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.store.ReadableMerchantStore;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableInventory extends InventoryEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String creationDate;

    private ReadableMerchantStore store;
    private String sku;
    private List<ReadableProductPrice> prices = new ArrayList<>();
    private String price;
}
