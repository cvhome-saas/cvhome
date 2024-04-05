package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.customer.address.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class CustomerEntity extends Customer implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @Email(message = "{messages.invalid.email}")
    @NotEmpty(message = "{NotEmpty.customer.emailAddress}")
    private String emailAddress;
    @Valid
    private Address billing;
    private Address delivery;
    private String gender;

    private String language;
    private String firstName;
    private String lastName;

    private String provider;//online, facebook ...


    private String storeCode;

    //@NotEmpty(message="{NotEmpty.customer.userName}")
    //can be email or anything else
    private String userName;

    private Double rating = 0D;
    private int ratingCount;


}
