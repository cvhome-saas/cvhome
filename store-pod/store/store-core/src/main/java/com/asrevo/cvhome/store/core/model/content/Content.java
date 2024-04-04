package com.asrevo.cvhome.store.core.model.content;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
@Deprecated
public abstract class Content implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    @NotEmpty
    private String name;
    private String contentType;

    public Content() {
    }

    public Content(String name) {
        this.name = name;
    }

    public Content(String name, String contentType) {
        this.name = name;
        this.contentType = contentType;
    }


}
