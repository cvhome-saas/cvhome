package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.model.customer.address.Address;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * RENTAL customer
 *
 * @author c.samson
 */
@Setter
@Getter
public class RentalOwner extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private Address address;
    private String emailAddress;

}
