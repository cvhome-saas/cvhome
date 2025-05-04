package com.asrevo.cvhome.catalog.model.manufacturer;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableManufacturer extends ManufacturerEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ManufacturerDescription description;
    private List<ManufacturerDescription> descriptions;
}
