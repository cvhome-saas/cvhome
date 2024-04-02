package com.asrevo.cvhome.store.core.model.content.page;

import com.asrevo.cvhome.store.core.model.content.common.ContentDescription;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableContentPage extends ContentPage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;


    private ContentDescription description;
    private String path;


}
