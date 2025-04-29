package com.asrevo.cvhome.catalog.model.product.attribute.api;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductAttributeList extends ReadableList {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableProductAttributeEntity> attributes = new ArrayList<>();
}
