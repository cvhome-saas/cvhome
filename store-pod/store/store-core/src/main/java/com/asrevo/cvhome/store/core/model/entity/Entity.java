package com.asrevo.cvhome.store.core.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id = 0L;

    public Entity() {
    }

    public Entity(Long id) {
        this.id = id;
    }

}
