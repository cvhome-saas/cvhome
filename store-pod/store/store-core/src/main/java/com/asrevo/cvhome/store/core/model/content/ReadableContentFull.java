package com.asrevo.cvhome.store.core.model.content;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Deprecated
@Getter
@Setter
public class ReadableContentFull extends Entity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean visible;
    private String contentType;
    private boolean isDisplayedInMenu;
    private List<ContentDescriptionEntity> descriptions = new ArrayList<ContentDescriptionEntity>();


}
