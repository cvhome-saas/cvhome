package com.asrevo.cvhome.store.core.model.content;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * System configuration settings for content management
 *
 * @author carlsamson
 */
@Getter
@Setter
public class ContentSettings implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String httpBasePath;
}
