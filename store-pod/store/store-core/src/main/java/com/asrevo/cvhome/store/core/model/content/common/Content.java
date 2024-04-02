package com.asrevo.cvhome.store.core.model.content.common;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Content extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean visible;
    private String contentType;


}
