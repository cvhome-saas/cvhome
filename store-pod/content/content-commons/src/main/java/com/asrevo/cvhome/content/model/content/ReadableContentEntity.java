package com.asrevo.cvhome.content.model.content;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableContentEntity extends ContentEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ContentDescriptionEntity description = null;
}
