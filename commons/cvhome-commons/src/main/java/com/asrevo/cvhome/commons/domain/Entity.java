package com.asrevo.cvhome.commons.domain;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Entity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Long id = 0L;

    public Entity() {}

    public Entity(Long id) {
        this.id = id;
    }
}
