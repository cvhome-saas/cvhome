package com.asrevo.cvhome.store.core.model.content.page;

import com.asrevo.cvhome.store.core.model.content.common.ContentDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ReadableContentPageFull extends ReadableContentPage {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ContentDescription> descriptions;

}
