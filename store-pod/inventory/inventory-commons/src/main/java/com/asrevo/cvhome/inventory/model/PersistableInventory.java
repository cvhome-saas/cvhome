package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * The body of the sku-addressed upsert: everything a merchant sets on one sku's stock and price. The sku itself is
 * the path; {@code productId} is stored so a catalog product delete can find its rows.
 *
 * @param quantityOrderMinimum smallest quantity one order may take; null keeps the current value (1 on create)
 * @param quantityOrderMaximum largest quantity one order may take, 0 for no limit; null keeps the current value
 */
public record PersistableInventory(Long productId,
                                   @NotNull @Min(0) Integer quantity,
                                   boolean available,
                                   @Min(0) Integer quantityOrderMinimum,
                                   @Min(0) Integer quantityOrderMaximum,
                                   @NotNull @Valid PersistablePrice price) implements Serializable {
}
