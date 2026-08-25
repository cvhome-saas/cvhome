package com.asrevo.cvhome.inventory.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A sku's price, already resolved: {@code finalPrice} is what the shopper pays today — the special amount while its
 * window is open, the regular amount otherwise. Amounts are raw numbers; every caller formats for its own locale.
 *
 * @param originalPrice    the regular amount
 * @param finalPrice       the amount charged today
 * @param discounted       whether {@code finalPrice} is the special amount
 * @param discountPercent  whole-number percentage off, {@code 0} when not discounted
 * @param specialAmount    the configured special amount, or null
 * @param specialStartDate first day of the special amount, or null for "already"
 * @param specialEndDate   day the special amount stops applying, or null for "never"
 */
public record SkuPrice(BigDecimal originalPrice, BigDecimal finalPrice, boolean discounted, int discountPercent,
                       BigDecimal specialAmount, LocalDate specialStartDate,
                       LocalDate specialEndDate) implements Serializable {
}
