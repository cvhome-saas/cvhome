package com.asrevo.cvhome.cache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;

/** A store's one row, for the integration test: the entity a cached read is built from. */
@Entity
@Table(name = "shop")
public class Shop implements StoreScoped {

    @Id
    private String store;

    private String name;

    protected Shop() {
    }

    public Shop(StoreMerchantId store, String name) {
        this.store = store.getId();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void rename(String name) {
        this.name = name;
    }

    @Override
    public StoreMerchantId scopedStore() {
        return new StoreMerchantId(store);
    }
}
