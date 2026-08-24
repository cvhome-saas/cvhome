package com.asrevo.cvhome.checkout.model.product;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.inventory.model.SkuInventory;

/**
 * A sku as checkout sees it: the catalog's product data next to the inventory service's stock and price. Composed by
 * {@code ProductDetailsComposer}; {@code inventory} is never null — a sku inventory knows nothing about arrives as
 * "not stocked" so a cart line can still render.
 */
public record ProductDetails(ReadableMinimalProduct product, SkuInventory inventory) {
}
