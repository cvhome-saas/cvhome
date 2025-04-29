package com.asrevo.cvhome.content.model.content.box;

import com.asrevo.cvhome.content.model.content.common.Content;
import com.asrevo.cvhome.content.model.content.common.ContentDescription;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableContentBox extends Content {

    private static final String BOX = "BOX";

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ContentDescription description;

    public ReadableContentBox() {
        super.setContentType(BOX);
    }
}
