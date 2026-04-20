package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOption extends ProductPropertyOption {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;

    private LanguageCode lang;

    private boolean variant;

    private List<ReadableProductOptionValue> optionValues = new ArrayList<>();

}
