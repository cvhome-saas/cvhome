package com.asrevo.cvhome.customer.model.customer.address;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddressLocation implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String postalCode;
    private String countryCode;
}
