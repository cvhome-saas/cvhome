package com.asrevo.cvhome.store.core.model.content;


import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

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
    @Serial
    private static final long serialVersionUID = 1L;
    private byte[] file;


}
