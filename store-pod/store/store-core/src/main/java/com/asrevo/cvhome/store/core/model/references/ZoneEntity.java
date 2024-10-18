package com.asrevo.cvhome.store.core.model.references;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ZoneEntity extends Entity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String countryCode;
    private String code;
}
