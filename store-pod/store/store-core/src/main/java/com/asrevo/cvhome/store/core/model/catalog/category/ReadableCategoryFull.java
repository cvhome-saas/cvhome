package com.asrevo.cvhome.store.core.model.catalog.category;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCategoryFull extends ReadableCategory {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<CategoryDescription> descriptions = new ArrayList<>();
}
