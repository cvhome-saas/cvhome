package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductVariant extends Entity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    //option name
    private String name;
    private String code;
    private List<ReadableProductVariantValue> options = new ArrayList<ReadableProductVariantValue>();


}
