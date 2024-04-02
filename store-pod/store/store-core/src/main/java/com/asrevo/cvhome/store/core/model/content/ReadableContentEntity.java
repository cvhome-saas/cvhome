package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableContentEntity extends ContentEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ContentDescriptionEntity description = null;

}
