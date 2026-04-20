package com.asrevo.cvhome.catalog.model.product.inventory;

import java.io.Serial;
import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableInventory extends InventoryEntity {

    /**
     * An inventory for a given product and possibly a given variant
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String store;

    @NotNull
    private Long productId;

    private Long variant;

    private List<PersistableProductPrice> prices;

}
