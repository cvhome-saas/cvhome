package com.asrevo.cvhome.content.model.content.page;

import com.asrevo.cvhome.content.model.content.common.Content;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContentPage extends Content {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean linkToMenu;
}
