package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;

/**
 * One sku's stock and price — what a storefront listing, the console grid and the checkout need to render or sell an
 * item. A sku with no inventory record is absent from a bulk answer, not present with zeros.
 *
 * @param sku                  the cross-service key
 * @param productId            the catalog product, informational only
 * @param available            the merchant's "may be sold" switch
 * @param canBePurchased       {@code available} and stock on hand
 * @param quantity             units on hand
 * @param quantityOrderMinimum smallest quantity one order may take
 * @param quantityOrderMaximum largest quantity one order may take, {@code 0} for no limit
 * @param price                the resolved price, or null when none is configured yet
 */
public record SkuInventory(String sku, Long productId, boolean available, boolean canBePurchased, int quantity,
                           int quantityOrderMinimum, int quantityOrderMaximum,
                           SkuPrice price) implements Serializable {
}
