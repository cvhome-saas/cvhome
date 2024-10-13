package com.asrevo.cvhome.store.core.model.shop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactForm {
    @NotEmpty private String name;
    @NotEmpty private String subject;
    @Email private String email;
    @NotEmpty private String comment;
}
