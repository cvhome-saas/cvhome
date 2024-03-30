package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.catalog.product.product.ProductEntity;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class ReadableMinimalProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ReadableDescription description;
    private ReadableProductPrice productPrice;
    private String finalPrice = "0";
    private String originalPrice = null;
    private ReadableImage image;
    private List<ReadableImage> images;


}
