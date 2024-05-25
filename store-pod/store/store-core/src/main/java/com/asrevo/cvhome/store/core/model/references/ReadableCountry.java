package com.asrevo.cvhome.store.core.model.references;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCountry extends CountryEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private List<ReadableZone> zones = new ArrayList<>();

}
