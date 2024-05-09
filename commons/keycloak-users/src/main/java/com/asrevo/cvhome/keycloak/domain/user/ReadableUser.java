package com.asrevo.cvhome.keycloak.domain.user;

import com.asrevo.cvhome.keycloak.domain.group.ReadableGroup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReadableUser extends UserEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String lastAccess;
    private String loginTime;
    private String store;
    private String userName;
    private boolean active;

    private List<ReadableGroup> groups = new ArrayList<>();

}
