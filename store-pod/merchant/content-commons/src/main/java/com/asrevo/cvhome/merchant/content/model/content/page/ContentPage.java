package com.asrevo.cvhome.merchant.content.model.content.page;

import java.io.Serial;

import com.asrevo.cvhome.merchant.content.model.content.common.Content;

import lombok.Getter;
import lombok.Setter;

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
