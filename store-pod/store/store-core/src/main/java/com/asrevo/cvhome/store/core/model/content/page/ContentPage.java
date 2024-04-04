package com.asrevo.cvhome.store.core.model.content.page;

import com.asrevo.cvhome.store.core.model.content.common.Content;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ContentPage extends Content {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private boolean linkToMenu;

}
