package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class ManufacturerEntity extends Manufacturer implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private int order;


}
