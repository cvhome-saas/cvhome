package com.asrevo.cvhome.content.model.content.page;

import com.asrevo.cvhome.content.model.content.common.ContentDescription;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PersistableContentPage extends ContentPage {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ContentDescription> descriptions;
}
