package com.asrevo.cvhome.content.model.content.page;

import java.io.Serial;
import java.util.List;

import com.asrevo.cvhome.content.model.content.common.ContentDescription;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableContentPage extends ContentPage {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ContentDescription> descriptions;

}
