package com.asrevo.cvhome.store.core.model.content;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
public class ReadableContentObject extends ObjectContent {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean isDisplayedInMenu;
    private String code;
    private Long id;
}
