package com.asrevo.cvhome.catalog.model.manufacturer;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableManufacturer extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private int order;

    private ManufacturerDescription description;

    private List<ManufacturerDescription> descriptions = new ArrayList<>();
}
