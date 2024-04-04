package com.asrevo.cvhome.store.core.model.catalog.product.inventory;

import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

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
