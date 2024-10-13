package com.asrevo.cvhome.store.core.model.content;

import com.asrevo.cvhome.commons.domain.Entity;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableContent extends Entity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String code;
    private boolean isDisplayedInMenu;
    private List<ObjectContent> descriptions = new ArrayList<>();
}
