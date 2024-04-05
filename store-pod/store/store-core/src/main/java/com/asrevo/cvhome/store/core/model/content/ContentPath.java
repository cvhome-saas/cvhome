package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ContentPath extends ContentName {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String path;

}
