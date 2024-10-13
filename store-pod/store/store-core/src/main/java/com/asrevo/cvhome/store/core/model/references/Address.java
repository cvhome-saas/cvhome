package com.asrevo.cvhome.store.core.model.references;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Address implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String stateProvince; // code
    private String country; // code
    private String address;
    private String postalCode;
    private String city;

    private boolean active = true;
}
