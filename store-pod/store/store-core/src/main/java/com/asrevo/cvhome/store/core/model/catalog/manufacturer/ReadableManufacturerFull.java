package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class ReadableManufacturerFull extends ReadableManufacturer {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<ManufacturerDescription> descriptions;

}
