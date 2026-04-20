package com.asrevo.cvhome.merchant.content.model.content.box;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.merchant.content.model.content.common.Content;
import com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription;

import lombok.Getter;
import lombok.Setter;

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

    private List<ContentDescription> descriptions;

    public ReadableContentBox() {
        super.setContentType(BOX);
    }

}
