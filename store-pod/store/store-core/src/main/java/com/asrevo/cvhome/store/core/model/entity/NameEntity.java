package com.asrevo.cvhome.store.core.model.entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * Used as an input request object where an entity name and or id is important
 *
 * @author carlsamson
 */
@Setter
@Getter
public class NameEntity extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @NotEmpty
    private String name;

}
