package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import com.asrevo.cvhome.commons.domain.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableManufacturerList extends ReadableList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableManufacturer> manufacturers = new ArrayList<ReadableManufacturer>();

}
