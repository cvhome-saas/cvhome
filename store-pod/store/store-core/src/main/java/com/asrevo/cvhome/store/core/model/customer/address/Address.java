package com.asrevo.cvhome.store.core.model.customer.address;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer or someone address
 *
 * @author carlsamson
 */
@Setter
@Getter
public class Address extends AddressLocation implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    // @NotEmpty(message="{NotEmpty.customer.firstName}")
    private String firstName;

    // @NotEmpty(message="{NotEmpty.customer.lastName}")
    private String lastName;

    private String bilstateOther;

    private String company;

    private String phone;
    private String address;
    private String city;

    private String stateProvince;
    private boolean billingAddress;

    private String latitude;
    private String longitude;

    private String zone; // code

    // @NotEmpty(message="{NotEmpty.customer.billing.country}")
    private String country; // code
}
