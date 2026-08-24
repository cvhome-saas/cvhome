package com.asrevo.cvhome.inventory.model.inventory;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;

import lombok.Getter;
import lombok.Setter;

/**
 * An inventory record keyed by sku — the inventory service's only link back to the catalog's product.
 *
 * <p>
 * {@code productId} is informational: the catalog owns the product, and inventory stores the id only so a console can
 * navigate back. Nothing in this service resolves it.
 * </p>
 */
@Setter
@Getter
public class PersistableInventory extends InventoryEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String store;

    /**
     * Filled from the path on the sku-addressed upsert, so it may be absent from the body — which is
     * why it is not {@code @NotEmpty}: bean validation runs before the controller can set it.
     */
    private String sku;

    private Long productId;

    private Long variant;

    private List<PersistableProductPrice> prices = new ArrayList<>();

}
