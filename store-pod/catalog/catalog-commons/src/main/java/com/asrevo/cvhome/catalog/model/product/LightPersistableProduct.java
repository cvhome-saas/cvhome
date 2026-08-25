package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * The console's inline edit: the two switches a merchant flips from the product list.
 */
@Getter
@Setter
public class LightPersistableProduct implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private boolean available;

    private boolean productShipeable;
}
