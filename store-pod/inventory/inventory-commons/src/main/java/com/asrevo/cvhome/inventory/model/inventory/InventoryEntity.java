package com.asrevo.cvhome.inventory.model.inventory;

import java.io.Serial;
import java.time.LocalDate;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InventoryEntity extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private int quantity;

    private String region;

    private String regionVariant;

    private String owner;

    private LocalDate dateAvailable;

    private boolean available;

    private int productQuantityOrderMin = 0;

    private int productQuantityOrderMax = 0;

}
