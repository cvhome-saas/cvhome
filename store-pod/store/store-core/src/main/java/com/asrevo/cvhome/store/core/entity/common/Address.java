package com.asrevo.cvhome.store.core.entity.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class Address implements Serializable {


    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String city;
    private String postalCode;
    private String stateProvince;
    private String zone;//code
    private String country;//code


}
