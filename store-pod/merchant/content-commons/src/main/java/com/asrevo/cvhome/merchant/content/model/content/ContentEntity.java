package com.asrevo.cvhome.merchant.content.model.content;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.Entity;

import lombok.Getter;
import lombok.Setter;

@Deprecated
@Getter
@Setter
public class ContentEntity extends Entity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    private String contentType = "BOX";

    private boolean isDisplayedInMenu;

    private boolean visible;

}
