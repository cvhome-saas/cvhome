package com.asrevo.cvhome.content.model.content;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Content implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @NotEmpty
    private String name;

    private String contentType;

    protected Content() {
    }

    protected Content(String name) {
        this.name = name;
    }

    protected Content(String name, String contentType) {
        this.name = name;
        this.contentType = contentType;
    }

}
