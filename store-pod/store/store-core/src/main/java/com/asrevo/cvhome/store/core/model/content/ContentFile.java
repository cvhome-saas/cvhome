package com.asrevo.cvhome.store.core.model.content;


import lombok.Getter;
import lombok.Setter;

/**
 * Model object used in webservice
 * when creatin files
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ContentFile extends ContentPath {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private byte[] file;


}
