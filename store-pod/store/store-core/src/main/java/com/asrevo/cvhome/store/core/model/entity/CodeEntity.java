package com.asrevo.cvhome.store.core.model.entity;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class CodeEntity extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;

}
