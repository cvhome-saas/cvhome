package com.asrevo.cvhome.customer.model.customer;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.customer.model.customer.address.CustomerAddress;

import lombok.Getter;
import lombok.Setter;

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
    private CustomerAddress billing;

    private CustomerAddress delivery;

    private String firstName;

    private String lastName;

    private String username;

    private String cuaExternalId;

}
