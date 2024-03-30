package com.asrevo.cvhome.store.core.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class EntityExists implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private boolean exists = false;

    public EntityExists() {

    }

    public EntityExists(boolean exists) {
        this.exists = exists;
    }

}
