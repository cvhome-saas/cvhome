package com.asrevo.cvhome.store.core.model.catalog.product.product;

import com.asrevo.cvhome.store.core.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.core.model.references.WeightUnitOfMeasure;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Specs weight dimension model and manufacturer
 *
 * @author carlsamson
 */
@Setter
@Getter
public class ProductSpecification implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private String model;
    private String manufacturer; // manufacturer code
    private DimensionUnitOfMeasure dimensionUnitOfMeasure;
    private WeightUnitOfMeasure weightUnitOfMeasure;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
