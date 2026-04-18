package com.asrevo.cvhome.uaa.domain.user;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String firstName;

    private String lastName;

    private String emailAddress;

    private String defaultLanguage;

    private String userName;

    private boolean active;

}
