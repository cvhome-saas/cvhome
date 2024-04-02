package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


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
    private static final long serialVersionUID = 1L;

    private String httpBasePath;

}
