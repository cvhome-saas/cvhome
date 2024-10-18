package com.asrevo.cvhome.store.core.model.references;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableZone extends ZoneEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String name;
}
