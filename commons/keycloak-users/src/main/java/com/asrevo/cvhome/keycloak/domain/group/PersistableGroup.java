package com.asrevo.cvhome.keycloak.domain.group;

import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * Object used for saving a group
 *
 * @author carlsamson
 */
@NoArgsConstructor
public class PersistableGroup extends GroupEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public PersistableGroup(String name) {
        super.setName(name);
    }

}
