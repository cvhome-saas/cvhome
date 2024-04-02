package com.asrevo.cvhome.store.core.model.references;


import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ZoneEntity extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String countryCode;
    private String code;

}
