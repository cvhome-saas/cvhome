package com.asrevo.cvhome.store.core.model.content;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersistableContent extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean isDisplayedInMenu;
    private List<ObjectContent> descriptions = new ArrayList<>();

}
