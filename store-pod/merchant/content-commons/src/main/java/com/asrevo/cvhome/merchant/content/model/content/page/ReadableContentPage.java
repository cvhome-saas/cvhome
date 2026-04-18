package com.asrevo.cvhome.merchant.content.model.content.page;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.merchant.content.model.content.common.ContentDescription;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableContentPage extends ContentPage {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ContentDescription description;

    private String path;

    private List<ContentDescription> descriptions;

}
