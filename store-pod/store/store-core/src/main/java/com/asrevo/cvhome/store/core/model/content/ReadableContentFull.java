package com.asrevo.cvhome.store.core.model.content;

import com.asrevo.cvhome.commons.domain.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Deprecated
@Getter
@Setter
public class ReadableContentFull extends Entity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean visible;
    private String contentType;
    private boolean isDisplayedInMenu;
    private List<ContentDescriptionEntity> descriptions = new ArrayList<>();


}
