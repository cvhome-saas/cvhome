package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Product extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean productShipeable = false;

    private boolean available;

    private boolean visible = true;

    private int sortOrder;

    private Instant dateAvailable;

    private Instant creationDate;

}
