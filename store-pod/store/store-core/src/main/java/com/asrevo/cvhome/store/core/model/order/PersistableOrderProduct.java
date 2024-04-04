package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductAttribute;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Setter
@Getter
public class PersistableOrderProduct extends OrderProductEntity implements
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private BigDecimal price;//specify final price
    private List<ProductAttribute> attributes;//may have attributes


}
