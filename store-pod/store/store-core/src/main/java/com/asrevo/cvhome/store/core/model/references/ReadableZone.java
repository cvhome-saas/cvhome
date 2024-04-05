package com.asrevo.cvhome.store.core.model.references;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableZone extends ZoneEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String name;

}
