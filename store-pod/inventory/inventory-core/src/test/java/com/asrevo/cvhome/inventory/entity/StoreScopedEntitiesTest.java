package com.asrevo.cvhome.inventory.entity;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static org.assertj.core.api.Assertions.assertThat;

class StoreScopedEntitiesTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    @Test
    void aStockRowAndAPriceRowNameTheirStore() {
        Inventory inventory = new Inventory();
        inventory.setStoreMerchantId(STORE);
        InventoryPrice price = new InventoryPrice();
        price.setStoreMerchantId(STORE);

        assertThat(inventory.scopedStore()).isEqualTo(STORE);
        assertThat(price.scopedStore()).isEqualTo(STORE);
    }
}
