package com.asrevo.cvhome.store.core.model.catalog.category;

import com.asrevo.cvhome.commons.domain.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCategoryList extends ReadableList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<ReadableCategory> categories = new ArrayList<>();

}
