package com.asrevo.cvhome.store.core.model.catalog.product;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Lightweight version of Persistable product
 *
 * @author carlsamson
 */
@Getter
@Setter
public class LightPersistableProduct implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String price;
    private boolean available;
    private boolean productShipeable;
    private int quantity;
}
