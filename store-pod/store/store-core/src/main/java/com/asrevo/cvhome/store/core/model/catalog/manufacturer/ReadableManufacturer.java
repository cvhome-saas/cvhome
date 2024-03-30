package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ReadableManufacturer extends ManufacturerEntity implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ManufacturerDescription description;

}
