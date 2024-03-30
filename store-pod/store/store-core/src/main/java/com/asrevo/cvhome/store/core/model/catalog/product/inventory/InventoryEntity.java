package com.asrevo.cvhome.store.core.model.catalog.product.inventory;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InventoryEntity extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int quantity;
    private String region;
    private String regionVariant;
    private String owner;
    private String dateAvailable;
    private boolean available;
    private int productQuantityOrderMin = 0;
    private int productQuantityOrderMax = 0;


}
