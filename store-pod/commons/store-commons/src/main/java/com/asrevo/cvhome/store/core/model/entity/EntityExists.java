package com.asrevo.cvhome.store.core.model.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EntityExists implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean exists = false;

    public EntityExists() {
    }

    public EntityExists(boolean exists) {
        this.exists = exists;
    }

}
