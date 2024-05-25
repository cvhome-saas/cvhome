package com.asrevo.cvhome.keycloak.domain.user;

import com.asrevo.cvhome.keycloak.domain.group.PersistableGroup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersistableUser extends UserEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String password;
    private String repeatPassword;
    private String store;
    private String userName;
    private boolean active;
    private List<PersistableGroup> groups = new ArrayList<>();
}
