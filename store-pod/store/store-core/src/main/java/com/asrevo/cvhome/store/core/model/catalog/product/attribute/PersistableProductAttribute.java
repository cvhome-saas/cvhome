package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.ProductAttributeEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Setter
@Getter
public class PersistableProductAttribute extends ProductAttributeEntity
        implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private BigDecimal productAttributeWeight;
    private BigDecimal productAttributePrice;
    private Long productId;
    private ProductPropertyOption option;
    private PersistableProductOptionValue optionValue;

}
