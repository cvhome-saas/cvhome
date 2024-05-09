package com.asrevo.cvhome.keycloak.domain.user;

import com.asrevo.cvhome.commons.domain.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReadableUserList extends ReadableList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ReadableUser> data = new ArrayList<>();
}
