package com.asrevo.cvhome.store.core.model.content;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Used for defining an image name and its path
 *
 * @author carlsamson
 */
@Getter
@Setter
public class ReadableImage implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private String path;

}
