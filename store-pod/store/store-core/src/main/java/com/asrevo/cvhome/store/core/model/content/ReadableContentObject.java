package com.asrevo.cvhome.store.core.model.content;

import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableContentObject extends ObjectContent {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean isDisplayedInMenu;
    private String code;
    private Long id;
}
