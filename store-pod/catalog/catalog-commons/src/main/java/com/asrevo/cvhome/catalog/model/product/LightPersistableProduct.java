package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

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
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean available;

    private boolean productShipeable;


}
