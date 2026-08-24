package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

import com.asrevo.cvhome.store.model.references.DimensionUnitOfMeasure;
import com.asrevo.cvhome.store.model.references.WeightUnitOfMeasure;

import lombok.Getter;
import lombok.Setter;

/**
 * The shipping box: dimensions and weight, with the store's units on the way out.
 */
@Getter
@Setter
public class ProductSpecification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigDecimal height;

    private BigDecimal weight;

    private BigDecimal length;

    private BigDecimal width;

    private DimensionUnitOfMeasure dimensionUnitOfMeasure;

    private WeightUnitOfMeasure weightUnitOfMeasure;
}
