package com.asrevo.cvhome.store.core.model.content;


import com.asrevo.cvhome.store.core.model.entity.ResourceUrlAccess;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
@Deprecated
public class ObjectContent extends ContentPath implements ResourceUrlAccess {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String slug;
    private String metaDetails;
    private String title;
    private String pageContent;
    private String language;


}
