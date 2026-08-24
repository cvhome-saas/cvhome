package com.asrevo.cvhome.inventory.model.inventory;

import java.io.Serial;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableInventory extends InventoryEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant creationDate;

    private ReadableMerchantStore store;

    private String sku;

    private Long productId;

    private List<ReadableProductPrice> prices = new ArrayList<>();

    private String price;

}
