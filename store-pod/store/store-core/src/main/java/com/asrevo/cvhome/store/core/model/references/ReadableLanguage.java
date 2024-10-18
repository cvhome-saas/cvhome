package com.asrevo.cvhome.store.core.model.references;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableLanguage implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String code;
    private int id;
}
