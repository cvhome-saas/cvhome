package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class Product extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean productShipeable = false;

    private boolean available;
    private boolean visible = true;

    private int sortOrder;
    private String dateAvailable;
    private String creationDate;


}
