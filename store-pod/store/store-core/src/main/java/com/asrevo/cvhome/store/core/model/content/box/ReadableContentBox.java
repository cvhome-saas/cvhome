package com.asrevo.cvhome.store.core.model.content.box;

import com.asrevo.cvhome.store.core.model.content.common.Content;
import com.asrevo.cvhome.store.core.model.content.common.ContentDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableContentBox extends Content {

    private static final String BOX = "BOX";
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private ContentDescription description;

    public ReadableContentBox() {
        super.setContentType(BOX);
    }

}
