package com.asrevo.cvhome.store.core.entity.content;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;


@Setter
@Getter
public class InputContentFile extends StaticContentFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private InputStream file;
    private String path;


}