package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

/**
 * A simple piece of content
 *
 * @author carlsamson
 */
@Setter
@Getter
@Deprecated
public class ReadableContent extends Content {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String content;

}
