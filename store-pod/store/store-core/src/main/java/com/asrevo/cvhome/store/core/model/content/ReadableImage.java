package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Used for defining an image name and its path
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ReadableImage implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String name;
    private String path;

}
