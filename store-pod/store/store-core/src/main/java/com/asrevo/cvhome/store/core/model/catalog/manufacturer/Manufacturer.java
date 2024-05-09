package com.asrevo.cvhome.store.core.model.catalog.manufacturer;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class Manufacturer extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;

}
