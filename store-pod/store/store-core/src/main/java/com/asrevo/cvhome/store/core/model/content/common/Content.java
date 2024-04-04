package com.asrevo.cvhome.store.core.model.content.common;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class Content extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean visible;
    private String contentType;


}
