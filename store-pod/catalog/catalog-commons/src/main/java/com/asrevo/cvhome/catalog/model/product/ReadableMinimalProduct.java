package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.product.ProductEntity;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableMinimalProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ReadableDescription description;

    private ReadableImage image;

    private List<ReadableImage> images;

}
