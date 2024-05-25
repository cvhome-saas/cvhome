package com.asrevo.cvhome.keycloak.domain.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String defaultLanguage;
    private String userName;
    private boolean active;
}
