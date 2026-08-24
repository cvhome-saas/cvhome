package com.asrevo.cvhome.checkout.model.product;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.inventory.model.availability.ReadableProductAvailability;
import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;

/**
 * Product, price and stock in one value — the shape checkout's cart and order code works against.
 *
 * <p>
 * Before the catalog/inventory split this record came back from one catalog call; now checkout composes it itself
 * (see {@code ProductDetailsComposer}) from catalog's product data and inventory's price/availability. The shape
 * survived the split so the mappers and populators downstream did not have to change.
 * </p>
 */
public record ProductDetails(ReadableMinimalProduct product, FinalPriceCalc price,
                             ReadableProductAvailability availability) {
}
