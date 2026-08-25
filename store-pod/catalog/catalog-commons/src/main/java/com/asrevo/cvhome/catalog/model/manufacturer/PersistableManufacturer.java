package com.asrevo.cvhome.catalog.model.manufacturer;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableManufacturer extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String code;

    private int order;

    private List<ManufacturerDescription> descriptions = new ArrayList<>();
}
