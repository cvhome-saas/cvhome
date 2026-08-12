package com.asrevo.cvhome.content.model.content.common;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

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
