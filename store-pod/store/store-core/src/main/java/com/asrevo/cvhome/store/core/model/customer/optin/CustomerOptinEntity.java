package com.asrevo.cvhome.store.core.model.customer.optin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;


@Setter
@Getter
public class CustomerOptinEntity extends CustomerOptin {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    @NotNull
    @Email
    private String email;

}
