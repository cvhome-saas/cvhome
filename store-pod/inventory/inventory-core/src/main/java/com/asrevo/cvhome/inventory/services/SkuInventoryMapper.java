package com.asrevo.cvhome.inventory.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.InventoryPrice;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;

/**
 * Entity to wire shape, including the one calculation this service does: which amount applies today.
 */
public final class SkuInventoryMapper {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private SkuInventoryMapper() {
    }

    public static SkuInventory toSkuInventory(Inventory inventory, LocalDate today) {
        SkuPrice price = inventory.defaultPrice().map(it -> toSkuPrice(it, today)).orElse(null);
        return new SkuInventory(inventory.getSku(), inventory.getProductId(), inventory.isAvailable(),
                inventory.canBePurchased(), inventory.getQuantity(), inventory.getQuantityOrderMinimum(),
                inventory.getQuantityOrderMaximum(), price);
    }

    public static SkuPrice toSkuPrice(InventoryPrice price, LocalDate today) {
        BigDecimal amount = price.getAmount();
        boolean discounted = price.isSpecialActiveOn(today);
        BigDecimal finalPrice = discounted ? price.getSpecialAmount() : amount;
        return new SkuPrice(amount, finalPrice, discounted, discounted ? percentOff(amount, finalPrice) : 0,
                price.getSpecialAmount(), price.getSpecialStartDate(), price.getSpecialEndDate());
    }

    private static int percentOff(BigDecimal amount, BigDecimal finalPrice) {
        if (amount.signum() <= 0) {
            return 0;
        }
        return HUNDRED.subtract(finalPrice.multiply(HUNDRED).divide(amount, 0, RoundingMode.DOWN)).intValue();
    }
}
