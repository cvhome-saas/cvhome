package com.asrevo.cvhome.inventory.entity;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a freshly constructed row already holds before anything is set on it.
 *
 * <p>
 * JPA needs the no-arg constructors, and Hibernate uses them for every row it hydrates — so the field initialisers
 * beside them are the state every loaded entity starts from. They are worth an assertion because each one is a
 * business default that would change behaviour silently if edited: a new sku is available and orderable one at a
 * time with no upper limit, a price is the default price at zero, and a reservation begins temporary rather than
 * committed.
 * </p>
 */
class EntityDefaultsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String SKU = "SKU-1";
    private static final String REF = "order-1";

    @Test
    void aNewInventoryRowIsAvailableOrderableOneAtATimeAndUnlimitedAbove() {
        Inventory inventory = new Inventory();

        assertThat(inventory.isAvailable()).isTrue();
        assertThat(inventory.getQuantityOrderMinimum()).isOne();
        assertThat(inventory.getQuantityOrderMaximum()).isZero();
        assertThat(inventory.getQuantity()).isZero();
        assertThat(inventory.getPrices()).isNotNull().isEmpty();
        assertThat(inventory.getAuditSection()).isNotNull();
    }

    @Test
    void theTwoArgConstructorIsTheOneTheServiceUsesToOpenAStockRow() {
        Inventory inventory = new Inventory(STORE, SKU);

        assertThat(inventory.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(inventory.getSku()).isEqualTo(SKU);
        assertThat(inventory.isAvailable()).isTrue();
    }

    @Test
    void aNewPriceRowIsTheDefaultPriceAtZero() {
        InventoryPrice price = new InventoryPrice();

        assertThat(price.isDefaultPrice()).isTrue();
        assertThat(price.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(price.getCode()).isNotBlank();
        assertThat(price.getSpecialAmount()).isNull();
    }

    @Test
    void aPriceBuiltFromItsInventoryInheritsThatRowsStore() {
        // The store is copied rather than joined through, because the price table is queried directly by store.
        InventoryPrice price = new InventoryPrice(new Inventory(STORE, SKU));

        assertThat(price.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(price.getInventory().getSku()).isEqualTo(SKU);
    }

    @Test
    void aNewReservationIsTemporaryUntilSomethingCommitsIt() {
        ProductReservation reservation = new ProductReservation();

        assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.TEMPORARY_RESERVED);
        assertThat(reservation.getLines()).isNotNull().isEmpty();
    }

    @Test
    void theTwoArgReservationConstructorCarriesTheStoreAndTheOrderReference() {
        ProductReservation reservation = new ProductReservation(STORE, REF);

        assertThat(reservation.getStoreMerchantId()).isEqualTo(STORE);
        assertThat(reservation.getRef()).isEqualTo(REF);
        assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.TEMPORARY_RESERVED);
    }

    @Test
    void aReservationLineJoinsAReservationToAStockRowWithAQuantity() {
        ProductReservation reservation = new ProductReservation(STORE, REF);
        Inventory inventory = new Inventory(STORE, SKU);

        assertThat(new ProductReservationLine()).isNotNull();
        ProductReservationLine line = new ProductReservationLine(reservation, inventory, 3);

        assertThat(line.getQuantity()).isEqualTo(3);
        assertThat(line.getInventory()).isSameAs(inventory);
        assertThat(line.getReservation()).isSameAs(reservation);
    }
}
