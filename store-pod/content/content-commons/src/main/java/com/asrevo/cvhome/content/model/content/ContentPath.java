package com.asrevo.cvhome.content.model.content;

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;

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
