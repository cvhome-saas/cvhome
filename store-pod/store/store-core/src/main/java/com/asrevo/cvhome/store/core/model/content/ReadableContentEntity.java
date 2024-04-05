package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
@Deprecated
public class ReadableContentEntity extends ContentEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ContentDescriptionEntity description = null;

}
