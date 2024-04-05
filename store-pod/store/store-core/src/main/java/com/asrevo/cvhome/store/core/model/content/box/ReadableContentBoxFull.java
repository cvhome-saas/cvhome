package com.asrevo.cvhome.store.core.model.content.box;

import com.asrevo.cvhome.store.core.model.content.common.ContentDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class ReadableContentBoxFull extends ReadableContentBox {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ContentDescription> descriptions;

}
