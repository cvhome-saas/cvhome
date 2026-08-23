package com.asrevo.cvhome.content.model.content;

import java.io.Serial;

import com.asrevo.cvhome.store.core.model.entity.ResourceUrlAccess;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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
