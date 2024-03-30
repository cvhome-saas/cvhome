package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableManufacturerList extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<ReadableManufacturer> manufacturers = new ArrayList<ReadableManufacturer>();

}
