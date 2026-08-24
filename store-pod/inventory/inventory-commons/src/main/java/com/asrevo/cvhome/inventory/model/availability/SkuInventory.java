package com.asrevo.cvhome.inventory.model.availability;

import java.io.Serializable;

import com.asrevo.cvhome.inventory.model.price.FinalPriceCalc;

import lombok.Builder;

/**
 * One sku's stock and price in one answer — what a storefront listing, a console grid or the checkout needs to render
 * or sell an item, without knowing how availability rows and prices relate internally.
 *
 * <p>
 * {@code price} is null when the sku has no price configured yet; {@code canBePurchased} folds quantity, status and
 * the available flag into the one boolean a caller acts on.
 * </p>
 */
@Builder
public record SkuInventory(String sku, boolean available, boolean canBePurchased, int quantity,
                           int quantityOrderMinimum, int quantityOrderMaximum,
                           FinalPriceCalc price) implements Serializable {
}
