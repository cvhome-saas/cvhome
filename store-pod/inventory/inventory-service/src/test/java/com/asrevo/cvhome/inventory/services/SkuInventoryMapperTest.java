package com.asrevo.cvhome.inventory.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.InventoryPrice;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.model.SkuPrice;

import static org.assertj.core.api.Assertions.assertThat;

class SkuInventoryMapperTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 24);

    private static final String SKU = "SKU-1";

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private static final BigDecimal SEVENTY_FIVE = new BigDecimal("75");

    @Test
    void regularPriceWhenNoSpecialAmount() {
        SkuPrice price = SkuInventoryMapper.toSkuPrice(price(HUNDRED, null, null, null), TODAY);

        assertThat(price.finalPrice()).isEqualByComparingTo(HUNDRED);
        assertThat(price.discounted()).isFalse();
        assertThat(price.discountPercent()).isZero();
    }

    @Test
    void specialAmountAppliesInsideItsWindow() {
        SkuPrice price = SkuInventoryMapper.toSkuPrice(
                price(HUNDRED, SEVENTY_FIVE, TODAY.minusDays(1), TODAY.plusDays(1)), TODAY);

        assertThat(price.finalPrice()).isEqualByComparingTo(SEVENTY_FIVE);
        assertThat(price.originalPrice()).isEqualByComparingTo(HUNDRED);
        assertThat(price.discounted()).isTrue();
        assertThat(price.discountPercent()).isEqualTo(25);
    }

    @Test
    void specialAmountStartsOnItsStartDateAndStopsOnItsEndDate() {
        assertThat(discountedOn(price(HUNDRED, SEVENTY_FIVE, TODAY, null))).isTrue();
        assertThat(discountedOn(price(HUNDRED, SEVENTY_FIVE, null, TODAY))).isFalse();
        assertThat(discountedOn(price(HUNDRED, SEVENTY_FIVE, TODAY.plusDays(1), null))).isFalse();
    }

    @Test
    void openEndedSpecialAmountAppliesUntilRemoved() {
        BigDecimal sixty = new BigDecimal("60");
        SkuPrice price = SkuInventoryMapper.toSkuPrice(price(new BigDecimal("80"), sixty, null, null), TODAY);

        assertThat(price.discounted()).isTrue();
        assertThat(price.finalPrice()).isEqualByComparingTo(sixty);
    }

    @Test
    void notPurchasableWithoutStockOrWhenUnavailable() {
        assertThat(SkuInventoryMapper.toSkuInventory(inventory(5, true), TODAY).canBePurchased()).isTrue();
        assertThat(SkuInventoryMapper.toSkuInventory(inventory(0, true), TODAY).canBePurchased()).isFalse();
        assertThat(SkuInventoryMapper.toSkuInventory(inventory(5, false), TODAY).canBePurchased()).isFalse();
    }

    @Test
    void skuWithoutPriceIsReportedWithNullPrice() {
        SkuInventory sku = SkuInventoryMapper.toSkuInventory(inventory(5, true), TODAY);

        assertThat(sku.sku()).isEqualTo(SKU);
        assertThat(sku.quantity()).isEqualTo(5);
        assertThat(sku.price()).isNull();
    }

    @Test
    void defaultPriceWinsOverOtherLegacyPriceRows() {
        BigDecimal ten = new BigDecimal("10");
        Inventory inventory = inventory(5, true);
        InventoryPrice other = new InventoryPrice(inventory);
        other.setId(1L);
        other.setDefaultPrice(false);
        other.setAmount(new BigDecimal("999"));
        InventoryPrice base = new InventoryPrice(inventory);
        base.setId(2L);
        base.setAmount(ten);
        inventory.getPrices().add(other);
        inventory.getPrices().add(base);

        assertThat(SkuInventoryMapper.toSkuInventory(inventory, TODAY).price().finalPrice())
                .isEqualByComparingTo(ten);
    }

    private static boolean discountedOn(InventoryPrice price) {
        return SkuInventoryMapper.toSkuPrice(price, TODAY).discounted();
    }

    private static Inventory inventory(int quantity, boolean available) {
        Inventory inventory = new Inventory(new StoreMerchantId("store-1"), SKU);
        inventory.setQuantity(quantity);
        inventory.setAvailable(available);
        return inventory;
    }

    private static InventoryPrice price(BigDecimal amount, BigDecimal special, LocalDate start, LocalDate end) {
        InventoryPrice price = new InventoryPrice(inventory(1, true));
        price.setAmount(amount);
        price.setSpecialAmount(special);
        price.setSpecialStartDate(start);
        price.setSpecialEndDate(end);
        return price;
    }
}
