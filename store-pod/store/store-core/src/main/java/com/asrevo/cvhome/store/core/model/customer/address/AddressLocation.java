package com.asrevo.cvhome.store.core.model.customer.address;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class AddressLocation implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String postalCode;
    private String countryCode;

}
