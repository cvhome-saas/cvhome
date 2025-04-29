package com.asrevo.cvhome.catalog.model.manufacturer;

import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableManufacturerFull extends ReadableManufacturer {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ManufacturerDescription> descriptions;
}
