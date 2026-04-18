package com.asrevo.cvhome.catalog.model.manufacturer;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

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
