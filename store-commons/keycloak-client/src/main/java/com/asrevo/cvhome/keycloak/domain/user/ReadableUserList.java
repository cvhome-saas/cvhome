package com.asrevo.cvhome.keycloak.domain.user;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Deprecated
public class ReadableUserList extends ReadableList<ReadableUser> {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableUser> content = new ArrayList<>();
}
