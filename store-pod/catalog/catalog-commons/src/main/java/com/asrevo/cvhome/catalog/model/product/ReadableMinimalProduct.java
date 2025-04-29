package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.catalog.model.product.product.ProductEntity;
import com.asrevo.cvhome.store.core.model.entity.ReadableDescription;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableMinimalProduct extends ProductEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private ReadableDescription description;
    private ReadableProductPrice productPrice;
    private String finalPrice = "0";
    private String originalPrice = null;
    private ReadableImage image;
    private List<ReadableImage> images;
}
