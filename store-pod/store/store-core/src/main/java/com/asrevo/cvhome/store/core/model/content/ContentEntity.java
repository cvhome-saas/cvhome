package com.asrevo.cvhome.store.core.model.content;


import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

@Deprecated
@Getter
@Setter
public class ContentEntity extends Entity {

    private static final long serialVersionUID = 1L;
    private String code;
    private String contentType = "BOX";
    private boolean isDisplayedInMenu;
    private boolean visible;
}
