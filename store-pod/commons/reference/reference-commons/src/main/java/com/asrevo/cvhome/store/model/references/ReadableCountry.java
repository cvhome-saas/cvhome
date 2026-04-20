package com.asrevo.cvhome.store.model.references;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
