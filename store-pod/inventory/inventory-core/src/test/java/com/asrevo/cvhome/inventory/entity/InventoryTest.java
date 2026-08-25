package com.asrevo.cvhome.inventory.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The few decisions the entities make themselves: which of several legacy price rows sells, whether a special amount
 * is live, and whether a reservation has run out.
 */
class InventoryTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String SKU = "SKU-1";

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 24);

    private static InventoryPrice price(Inventory inventory, Long id, boolean defaultPrice) {
        InventoryPrice price = new InventoryPrice(inventory);
        price.setId(id);
        price.setDefaultPrice(defaultPrice);
        inventory.getPrices().add(price);
        return price;
    }

    @Test
    void noPriceRowsMeansNoDefaultPrice() {
        assertThat(new Inventory(STORE, SKU).defaultPrice()).isEmpty();
    }

    @Test
    void withoutAFlaggedDefaultTheOldestRowSells() {
        Inventory inventory = new Inventory(STORE, SKU);
        InventoryPrice unsaved = price(inventory, null, false);
        InventoryPrice newer = price(inventory, 20L, false);
        InventoryPrice oldest = price(inventory, 10L, false);

        assertThat(inventory.defaultPrice()).contains(oldest);
        assertThat(inventory.defaultPrice()).isNotIn(unsaved, newer);
    }

    @Test
    void unsavedRowsSortLastSoASavedRowWins() {
        Inventory inventory = new Inventory(STORE, SKU);
        price(inventory, null, false);
        InventoryPrice saved = price(inventory, 5L, false);

        assertThat(inventory.defaultPrice()).contains(saved);
    }

    @Test
    void newInventoryIsAvailableWithNoStockAndNoUpperLimit() {
        Inventory inventory = new Inventory(STORE, SKU);

        assertThat(inventory.isAvailable()).isTrue();
        assertThat(inventory.canBePurchased()).isFalse();
        assertThat(inventory.getQuantityOrderMinimum()).isEqualTo(1);
        assertThat(inventory.getQuantityOrderMaximum()).isZero();
    }

    @Test
    void zeroOrNegativeSpecialAmountIsNeverActive() {
        InventoryPrice price = new InventoryPrice(new Inventory(STORE, SKU));
        price.setSpecialAmount(BigDecimal.ZERO);
        assertThat(price.isSpecialActiveOn(TODAY)).isFalse();
        price.setSpecialAmount(new BigDecimal("-1"));
        assertThat(price.isSpecialActiveOn(TODAY)).isFalse();
        price.setSpecialAmount(BigDecimal.ONE);
        assertThat(price.isSpecialActiveOn(TODAY)).isTrue();
        assertThat(price.getCode()).isEqualTo(InventoryPrice.DEFAULT_CODE);
    }

    @Test
    void reservationKnowsWhatItHoldsAndWhenItRanOut() {
        Instant now = Instant.parse("2026-08-24T10:00:00Z");
        ProductReservation reservation = new ProductReservation(STORE, "order-1");
        reservation.setExpireAt(now);
        Inventory inventory = new Inventory(STORE, SKU);
        reservation.addLine(inventory, 2);

        assertThat(reservation.holds(SKU)).isTrue();
        assertThat(reservation.holds("other")).isFalse();
        assertThat(reservation.getLines().getFirst().getInventory()).isSameAs(inventory);
        assertThat(reservation.getLines().getFirst().getReservation()).isSameAs(reservation);
        assertThat(reservation.getLines().getFirst().getQuantity()).isEqualTo(2);
        assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.TEMPORARY_RESERVED);
        assertThat(reservation.isExpired(now)).as("expiry is exclusive").isFalse();
        assertThat(reservation.isExpired(now.plusMillis(1))).isTrue();
    }
}
